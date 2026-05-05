# SmartKB

SmartKB is a Spring Boot based enterprise knowledge-base and AI agent workspace prototype. It combines document ingestion, vector retrieval, RAG answer generation, agent orchestration, tool execution, MCP-style tool bridging, trace events, and basic runtime metrics.

## Features

- Knowledge-base management with document upload and parsing
- Apache Tika based text extraction
- Semantic chunking and embedding storage
- PostgreSQL + pgvector vector retrieval
- Query rewrite, hybrid recall, lightweight rerank, and citation
- RAG answer generation with extractive fallback
- Agent orchestration with LLM Router + rule fallback
- Tool registry, tool schema, argument validation, and execution result protocol
- MCP-style tool descriptor and bridge endpoints
- Agent trace events, task status, and basic metrics
- Static web console for app, knowledge base, Agent, and tool debugging

## Tech Stack

- Java 17
- Spring Boot 3.4
- Spring Web / Validation / JPA / JDBC
- PostgreSQL + pgvector
- LangChain4j OpenAI-compatible models
- Apache Tika
- Tailwind CSS CDN + Lucide icons for the static console

## Runtime Requirements

- JDK 17+
- Maven 3.8+
- PostgreSQL with pgvector extension
- OpenAI-compatible embedding/chat model endpoint

## Configuration

Main configuration is in `src/main/resources/application.yml`.

For local startup, configure these environment variables instead of committing secrets:

```bash
EMBEDDING_API_KEY=your_embedding_api_key
EMBEDDING_BASE_URL=https://api.example.com/openai
EMBEDDING_DIMENSIONS=1024
CHAT_API_KEY=your_chat_api_key
CHAT_BASE_URL=https://api.example.com/openai
CHAT_MODEL_NAME=gpt-4o-mini
CHAT_TEMPERATURE=0.2
```

Database defaults in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/postgres
    username: postgres
    password: 12345zxcvb
```

Adjust these values locally as needed.

## Start

```bash
mvn spring-boot:run
```

Then open:

```text
http://localhost:8082
```

## Main API Endpoints

### Knowledge Base

- `POST /api/knowledge`
- `GET /api/knowledge`
- `POST /api/knowledge/{kbId}/upload`
- `POST /api/knowledge/{kbId}/hit-test`

### Agent

- `POST /api/agent/route`

Example:

```json
{
  "input": "如何上传知识库文档？",
  "kbId": 1,
  "applicationId": null
}
```

### Tools

- `GET /api/tools`
- `GET /api/tools/mcp`
- `POST /api/tools/execute`

Example:

```json
{
  "toolName": "knowledge.answer",
  "arguments": {
    "query": "如何上传知识库文档？",
    "kbId": 1,
    "topN": 5,
    "threshold": 0.35
  }
}
```

### MCP Bridge

- `GET /api/mcp/tools`
- `POST /api/mcp/invoke`

### Metrics

- `GET /api/metrics/agent`

## Agent Flow

```text
User input
→ LLM Router / rule fallback
→ Agent plan
→ Tool schema validation
→ Tool execution
→ Trace events
→ Task status
→ Metrics snapshot
```

For knowledge QA:

```text
Question
→ knowledge.answer
→ query rewrite
→ vector recall + keyword recall
→ rerank
→ citation
→ answer generation
→ answer + citations + contexts
```

## Notes

- `postgres-data/` and `postgres_data/` are local runtime database directories and are ignored by Git.
- `面试说明书.md` is a local interview note and is ignored by Git.
- Do not commit API keys or local secrets. Use environment variables instead.
