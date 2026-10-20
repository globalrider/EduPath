package com.example.edupath.demo.service.impl;

import com.example.edupath.demo.dto.*;
import com.example.edupath.demo.exception.ResourceNotFoundException;
import com.example.edupath.demo.model.*;
import com.example.edupath.demo.repository.RoadmapEdgeRepository;
import com.example.edupath.demo.repository.RoadmapNodeRepository;
import com.example.edupath.demo.repository.RoadmapRepository;
import com.example.edupath.demo.repository.UserRepository;
import com.example.edupath.demo.service.RoadmapService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoadmapServiceImpl implements RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapNodeRepository nodeRepository;
    private final RoadmapEdgeRepository edgeRepository;
    private final UserRepository userRepository;

    public RoadmapServiceImpl(
            RoadmapRepository roadmapRepository,
            RoadmapNodeRepository nodeRepository,
            RoadmapEdgeRepository edgeRepository,
            UserRepository userRepository
    ) {
        this.roadmapRepository = roadmapRepository;
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
        this.userRepository = userRepository;
    }

    @Override
    public RoadmapResponse generateAiRoadmap(String userEmail, GenerateRoadmapRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        String goal = request.getGoal();
        String title = (request.getTitle() != null && !request.getTitle().isBlank())
                ? request.getTitle()
                : "AI Career Path: " + goal;

        Roadmap roadmap = new Roadmap(user, goal, title);
        Roadmap savedRoadmap = roadmapRepository.save(roadmap);

        // Generate tailored milestone nodes and prerequisite graph edges
        List<RoadmapNode> generatedNodes = generateNodesForGoal(savedRoadmap, goal);
        List<RoadmapNode> savedNodes = nodeRepository.saveAll(generatedNodes);

        List<RoadmapEdge> generatedEdges = generateEdgesForNodes(savedNodes, goal);
        List<RoadmapEdge> savedEdges = edgeRepository.saveAll(generatedEdges);

        List<RoadmapNodeDto> nodeDtos = savedNodes.stream()
                .map(RoadmapNodeDto::fromEntity)
                .collect(Collectors.toList());

        List<RoadmapEdgeDto> edgeDtos = savedEdges.stream()
                .map(RoadmapEdgeDto::fromEntity)
                .collect(Collectors.toList());

        return RoadmapResponse.fromEntity(savedRoadmap, nodeDtos, edgeDtos);
    }

    @Override
    public RoadmapResponse createManualRoadmap(String userEmail, RoadmapRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        String title = (request.getTitle() != null && !request.getTitle().isBlank())
                ? request.getTitle()
                : "Roadmap: " + request.getGoal();

        Roadmap roadmap = new Roadmap(user, request.getGoal(), title);
        Roadmap saved = roadmapRepository.save(roadmap);

        return RoadmapResponse.fromEntity(saved, new ArrayList<>(), new ArrayList<>());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoadmapResponse> getRoadmaps(String userEmail) {
        List<Roadmap> roadmaps;
        if (userEmail != null && !userEmail.isBlank()) {
            userRepository.findByEmail(userEmail).ifPresent(u -> {});
            roadmaps = roadmapRepository.findAll();
        } else {
            roadmaps = roadmapRepository.findAll();
        }

        return roadmaps.stream().map(r -> {
            List<RoadmapNodeDto> nodes = nodeRepository.findByRoadmap_RoadmapIdOrderBySequenceOrderAsc(r.getRoadmapId())
                    .stream().map(RoadmapNodeDto::fromEntity).collect(Collectors.toList());
            List<RoadmapEdgeDto> edges = edgeRepository.findByRoadmapId(r.getRoadmapId())
                    .stream().map(RoadmapEdgeDto::fromEntity).collect(Collectors.toList());
            return RoadmapResponse.fromEntity(r, nodes, edges);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoadmapResponse getRoadmapById(Long roadmapId) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found with ID: " + roadmapId));

        List<RoadmapNodeDto> nodes = nodeRepository.findByRoadmap_RoadmapIdOrderBySequenceOrderAsc(roadmapId)
                .stream().map(RoadmapNodeDto::fromEntity).collect(Collectors.toList());

        List<RoadmapEdgeDto> edges = edgeRepository.findByRoadmapId(roadmapId)
                .stream().map(RoadmapEdgeDto::fromEntity).collect(Collectors.toList());

        return RoadmapResponse.fromEntity(roadmap, nodes, edges);
    }

    @Override
    public RoadmapNodeDto addNode(Long roadmapId, String userEmail, boolean isAdmin, RoadmapNodeDto nodeDto) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found with ID: " + roadmapId));

        if (!isAdmin && !roadmap.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("You can only add nodes to your own career roadmap");
        }

        RoadmapNode node = new RoadmapNode(
            roadmap,
            nodeDto.getTitle(),
            nodeDto.getDescription(),
            nodeDto.getSemester(),
            nodeDto.getSequenceOrder()
        );

        RoadmapNode saved = nodeRepository.save(node);
        return RoadmapNodeDto.fromEntity(saved);
    }

    @Override
    public RoadmapEdgeDto addEdge(Long roadmapId, String userEmail, boolean isAdmin, RoadmapEdgeDto edgeDto) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found with ID: " + roadmapId));

        if (!isAdmin && !roadmap.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("You can only configure edges for your own career roadmap");
        }

        RoadmapNode sourceNode = nodeRepository.findById(edgeDto.getSourceNodeId())
                .orElseThrow(() -> new ResourceNotFoundException("Source node not found with ID: " + edgeDto.getSourceNodeId()));

        RoadmapNode targetNode = nodeRepository.findById(edgeDto.getTargetNodeId())
                .orElseThrow(() -> new ResourceNotFoundException("Target node not found with ID: " + edgeDto.getTargetNodeId()));

        RoadmapEdge edge = new RoadmapEdge(sourceNode, targetNode);
        RoadmapEdge saved = edgeRepository.save(edge);

        return RoadmapEdgeDto.fromEntity(saved);
    }

    @Override
    public void deleteRoadmap(Long roadmapId, String userEmail, boolean isAdmin) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found with ID: " + roadmapId));

        if (!isAdmin && !roadmap.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("You can only delete your own career roadmap");
        }

        edgeRepository.deleteByRoadmapId(roadmapId);
        nodeRepository.deleteByRoadmapId(roadmapId);
        roadmapRepository.delete(roadmap);
    }

    @Override
    public void deleteNode(Long nodeId, String userEmail, boolean isAdmin) {
        RoadmapNode node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap node not found with ID: " + nodeId));

        if (!isAdmin && !node.getRoadmap().getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("You can only delete nodes from your own career roadmap");
        }

        edgeRepository.deleteByNodeId(nodeId);
        nodeRepository.delete(node);
    }

    @Override
    public void deleteEdge(Long sourceNodeId, Long targetNodeId, String userEmail, boolean isAdmin) {
        RoadmapEdgeId edgeId = new RoadmapEdgeId(sourceNodeId, targetNodeId);
        RoadmapEdge edge = edgeRepository.findById(edgeId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap edge not found between " + sourceNodeId + " and " + targetNodeId));

        if (!isAdmin && !edge.getSourceNode().getRoadmap().getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("You can only remove edges from your own career roadmap");
        }

        edgeRepository.delete(edge);
    }

    private List<RoadmapNode> generateNodesForGoal(Roadmap roadmap, String goal) {
        List<RoadmapNode> nodes = new ArrayList<>();
        String lowerGoal = goal.toLowerCase();

        if (lowerGoal.contains("ai") || lowerGoal.contains("machine learning") || lowerGoal.contains("data")) {
            nodes.add(new RoadmapNode(roadmap, "Python & Linear Algebra Fundamentals", "Master NumPy, Pandas, Vector calculus, and probability.", 1, 1));
            nodes.add(new RoadmapNode(roadmap, "Data Preprocessing & Visualization", "Feature engineering, Matplotlib, Seaborn, and exploratory analysis.", 2, 2));
            nodes.add(new RoadmapNode(roadmap, "Machine Learning Algorithms", "Supervised/unsupervised algorithms, scikit-learn, cross-validation.", 3, 3));
            nodes.add(new RoadmapNode(roadmap, "Deep Learning & Neural Networks", "PyTorch, CNNs for computer vision, RNNs for sequences.", 4, 4));
            nodes.add(new RoadmapNode(roadmap, "LLMs & Generative AI", "Transformers, Hugging Face, Prompt Engineering, LangChain, RAG.", 5, 5));
            nodes.add(new RoadmapNode(roadmap, "MLOps & Cloud Deployment", "Deploy models with FastAPI, Docker, and AWS SageMaker.", 6, 6));
        } else if (lowerGoal.contains("cloud") || lowerGoal.contains("devops")) {
            nodes.add(new RoadmapNode(roadmap, "Linux Systems & Bash Scripting", "Command line mastery, file systems, permissions, automation.", 1, 1));
            nodes.add(new RoadmapNode(roadmap, "Networking & Security Essentials", "TCP/IP, DNS, SSL/TLS, reverse proxies, and firewalls.", 2, 2));
            nodes.add(new RoadmapNode(roadmap, "Git & CI/CD Automation", "GitHub Actions, automated testing, version control release pipelines.", 3, 3));
            nodes.add(new RoadmapNode(roadmap, "Containerization with Docker", "Containerizing microservices, multi-stage builds, and Docker Compose.", 4, 4));
            nodes.add(new RoadmapNode(roadmap, "Kubernetes Cluster Orchestration", "Deployments, Services, Ingress, ConfigMaps, Helm charts.", 5, 5));
            nodes.add(new RoadmapNode(roadmap, "Infrastructure as Code (Terraform)", "Declarative cloud provisioning on AWS/GCP with Terraform.", 6, 6));
        } else {
            // Default: Full Stack Web & Software Engineering
            nodes.add(new RoadmapNode(roadmap, "Programming & DSA Fundamentals", "Master core C/Java, pointers, arrays, lists, recursion, sorting.", 1, 1));
            nodes.add(new RoadmapNode(roadmap, "Object-Oriented Design & Java OOP", "Classes, interfaces, inheritance, polymorphism, design patterns.", 2, 2));
            nodes.add(new RoadmapNode(roadmap, "Relational Database Design & MySQL", "ACID properties, 3NF schema normalization, indexing, joins.", 3, 3));
            nodes.add(new RoadmapNode(roadmap, "Spring Boot & RESTful Architecture", "Controllers, Services, Repositories, JPA/Hibernate mapping.", 4, 4));
            nodes.add(new RoadmapNode(roadmap, "JWT Authentication & Role Authorization", "Stateless security filter chains, BCrypt hashing, token validation.", 5, 5));
            nodes.add(new RoadmapNode(roadmap, "Modern Frontend UI (React & Tailwind)", "Component architecture, state management, REST API integration.", 6, 6));
            nodes.add(new RoadmapNode(roadmap, "Docker & Production Deployment", "Containerization, cloud environment setup, monitoring.", 7, 7));
        }

        return nodes;
    }

    private List<RoadmapEdge> generateEdgesForNodes(List<RoadmapNode> nodes, String goal) {
        List<RoadmapEdge> edges = new ArrayList<>();
        if (nodes.size() < 2) return edges;

        // Connect sequential prerequisite dependency chains
        for (int i = 0; i < nodes.size() - 1; i++) {
            edges.add(new RoadmapEdge(nodes.get(i), nodes.get(i + 1)));
        }

        // If default Full Stack, also add an edge from Database (index 2) directly to JWT & Security (index 4)
        if (nodes.size() >= 5) {
            edges.add(new RoadmapEdge(nodes.get(2), nodes.get(4)));
        }

        return edges;
    }
}
