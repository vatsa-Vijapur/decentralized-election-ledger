# Decentralized Election Ledger

An in-memory blockchain application leveraging SHA-256 and Merkle Trees to secure voting data against internal database tampering.

## Prerequisites
* Java 11 or higher
* Any standard web browser (Chrome/Edge)

## System Architecture
```mermaid
graph TD
    A([💻 Client Browser <br/> Vanilla JS/HTML])
    B[🌐 API Gateway <br/> com.sun.net.httpserver]
    C(JSON Extraction)
    D{🛡️ HashSet Validator <br/> O1 Check}
    E[(📥 In-Memory Buffer <br/> ArrayList)]
    F[🌳 Merkle Tree Engine <br/> SHA-256 Recursion]
    G[(⛓️ Cryptographic Linked List <br/> Blockchain Ledger)]

    A -- HTTP POST /api/vote --> B
    B --> C
    C --> D
    D -- Valid Vote --> E
    E -- Batch Limit Reached --> F
    F -- Block Sealing --> G

    style A fill:#1e293b,stroke:#3b82f6,stroke-width:2px,color:#fff
    style B fill:#1e293b,stroke:#64748b,stroke-width:2px,color:#fff
    style C fill:#1e293b,stroke:#64748b,stroke-width:2px,color:#fff
    style D fill:#1e293b,stroke:#f59e0b,stroke-width:2px,color:#fff
    style E fill:#1e293b,stroke:#64748b,stroke-width:2px,color:#fff
    style F fill:#1e293b,stroke:#8b5cf6,stroke-width:2px,color:#fff
    style G fill:#1e293b,stroke:#10b981,stroke-width:2px,color:#fff
```
