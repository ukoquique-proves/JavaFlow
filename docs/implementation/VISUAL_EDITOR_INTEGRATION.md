# 🎨 Integrating a Visual Workflow Editor (Flowable Modeler)

This document outlines the plan to add a visual, web-based workflow editor to the JavaFlow ecosystem by integrating the official **Flowable Modeler**.

## 🎯 Goal

To provide a user-friendly, drag-and-drop interface for creating and editing BPMN workflows, similar to the experience in products like n8n, without rewriting the core JavaFlow application.

## 🏗️ Architectural Approach

We will run the Flowable Modeler as a separate, containerized service that connects to the same PostgreSQL database as the main JavaFlow application. This approach is:
- **Low-Impact**: It requires no changes to the existing JavaFlow backend code.
- **Decoupled**: The Modeler and the JavaFlow application are independent services that share a common data source (the database).
- **Official & Robust**: We leverage the official, feature-rich editor provided by the Flowable team.

### New Architecture Diagram

```
┌───────────────────────────────┐      ┌───────────────────────────────┐
│        Flowable Modeler       │      │         JavaFlow (Vaadin)       │
│      (Visual Workflow Editor) │      │        (Management UI)        │
│         (Runs on port 8088)   │      │         (Runs on port 8080)     │
└───────────────┬───────────────┘      └───────────────┬───────────────┘
                │ (Writes BPMN models)                 │ (Reads & Executes BPMN models)
                └─────────────┐  ┌───────────────┘
                              ↓  ↓
┌─────────────────────────────────────────────────────────────┐
│                       PostgreSQL Database                     │
│ ┌───────────────────┐      ┌────────────────────────────────┐ │
│ │ Flowable Tables   │      │      JavaFlow Tables           │ │
│ │ (ACT_RE_*, etc.)  │      │ (workflows, users, logs)       │ │
│ └───────────────────┘      └────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

---

## 📋 Implementation Steps

### Step 1: Update `docker-compose.yml`

I will add a new service definition for the Flowable Modeler to the existing `docker-compose.yml` file.

```yaml
services:
  # ... (existing postgres and javaflow services)

  flowable-modeler:
    image: flowable/flowable-modeler:latest
    container_name: flowable-modeler
    ports:
      - "8088:8088"
    environment:
      - FLOWABLE_COMMON_APP_IDM_ADMIN_USER=admin
      - FLOWABLE_COMMON_APP_IDM_ADMIN_PASSWORD=test
      - SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/javaflow_db
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=postgres
    depends_on:
      - postgres
```

### Step 2: Update Documentation

I will update the project's main `README.md` and other relevant documentation to include instructions on how to access and use the new Flowable Modeler.

- **New URL**: The Modeler will be accessible at `http://localhost:8088/flowable-modeler`.
- **Default Credentials**: `admin` / `test`.

### Step 3: (Optional) Add Link from Vaadin UI

To improve user experience, I can add a button or link within the existing JavaFlow Vaadin UI that opens the Flowable Modeler in a new browser tab. This will make the integration feel seamless.

---

## ✅ Expected Outcome

After these steps are completed, you will be able to:
1.  Launch the entire ecosystem with a single `docker-compose up` command.
2.  Open `http://localhost:8088/flowable-modeler` to visually create and edit workflows.
3.  See those new workflows appear automatically in the JavaFlow Vaadin UI at `http://localhost:8080`.
4.  Execute the visually-created workflows directly from the JavaFlow application.
