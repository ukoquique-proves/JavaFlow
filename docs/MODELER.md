# 📖 How to Use the Visual Workflow Editor (Flowable Modeler)

This guide explains the two-step process for creating a workflow in the visual editor and then making it available for execution within the JavaFlow application.

## 🎯 The Core Concept: Design vs. Execution

It's important to understand the separation of concerns:

1.  **Flowable Modeler (The Designer)**: A visual tool for **designing and modeling** BPMN processes. The output is a BPMN 2.0 XML definition.
2.  **JavaFlow Application (The Engine)**: The application responsible for **managing and executing** workflows. It treats the BPMN XML from the Modeler as the "source code" for a workflow.

You first design the workflow visually, then you "register" that design with the JavaFlow application so it can be executed.

---

## Step 1: Design Your Workflow in the Flowable Modeler

1.  **Start the Ecosystem**: Make sure all services are running via Docker:
    ```bash
    docker-compose up --build
    ```

2.  **Access the Modeler**: Open your web browser and navigate to:
    - **URL**: [http://localhost:8088/flowable-modeler](http://localhost:8088/flowable-modeler)

3.  **Log In**: Use the default administrator credentials:
    - **Username**: `admin`
    - **Password**: `test`

4.  **Create a New Process**:
    - Navigate to the **Processes** section.
    - Click the **Create Process** button.
    - Give your process a name (e.g., "My New Automated Process") and a key.
    - Use the drag-and-drop interface to build your workflow. Add start events, service tasks, gateways, and end events.

5.  **Configure Service Tasks**:
    - For any `Service Task` node, you must specify which Java class will execute it.
    - Select the task, and in the properties panel, set the **Delegate Expression** to the name of a Spring bean in the JavaFlow application.
    - For example: `${logTask}` or `${sendMessageTask}`.

6.  **Get the BPMN XML**:
    - Once your model is complete and saved, click the **XML** button in the editor's toolbar.
    - This will show you the raw BPMN 2.0 XML definition of your visual model.
    - **Copy this entire XML content to your clipboard.** This is the "source code" you will give to JavaFlow.

---

## Step 2: Register the Workflow in the JavaFlow Application

Now that you have the BPMN XML, you need to create a `Workflow` entity in the JavaFlow application so it can be managed and executed.

1.  **Access the JavaFlow UI**:
    - Open the main application UI at [http://localhost:8080](http://localhost:8080).

2.  **Navigate to the Workflow List**:
    - Go to the "Workflows" -> "Workflow List" section from the side navigation menu.

3.  **Create a New Workflow**:
    - Click the button to create a new workflow (this may be a "New Workflow" button or similar, depending on the UI implementation).
    - A form will appear asking for details about the new workflow.

4.  **Paste the BPMN XML**:
    - In the form, provide a name and description for your workflow.
    - In the field for the "BPMN XML" or "Definition", **paste the XML content** you copied from the Flowable Modeler.

5.  **Save and Activate**:
    - Save the new workflow. It will be created in a `DRAFT` status.
    - From the workflow list, find your new workflow and click the "Activate" button. This will deploy it to the Flowable engine and make it ready for execution.

Your visually designed workflow is now fully integrated and executable within the JavaFlow application.
