/**
 * Workflow use cases implementing the application layer business logic.
 * 
 * <p>This package contains use case classes that orchestrate workflow operations
 * following the Clean Architecture pattern. Each use case represents a single
 * business operation and coordinates between the domain layer and infrastructure.</p>
 * 
 * <h2>Available Use Cases</h2>
 * <ul>
 *   <li>{@link com.javaflow.application.workflow.CreateWorkflowUseCase} - Creates a new workflow definition</li>
 *   <li>{@link com.javaflow.application.workflow.ActivateWorkflowUseCase} - Activates a workflow for execution</li>
 *   <li>{@link com.javaflow.application.workflow.ExecuteWorkflowUseCase} - Executes a workflow instance</li>
 * </ul>
 * 
 * <h2>Design Pattern</h2>
 * <p>All use cases follow the Command pattern:</p>
 * <pre>{@code
 * // 1. Create a command with input data
 * CreateWorkflowCommand command = CreateWorkflowCommand.of(
 *     "My Workflow",
 *     "Description",
 *     bpmnXml,
 *     userId
 * );
 * 
 * // 2. Execute the use case
 * WorkflowResult result = createWorkflowUseCase.execute(command);
 * 
 * // 3. Handle the result
 * if (result.isSuccess()) {
 *     Workflow workflow = result.workflow();
 *     // Process successful result
 * }
 * }</pre>
 * 
 * <h2>Key Features</h2>
 * <ul>
 *   <li><strong>Transactional:</strong> Each use case runs in a transaction</li>
 *   <li><strong>Event Publishing:</strong> Domain events are published automatically</li>
 *   <li><strong>Validation:</strong> Input validation before execution</li>
 *   <li><strong>Error Handling:</strong> Consistent error handling with domain exceptions</li>
 * </ul>
 * 
 * @see com.javaflow.application.common.UseCase
 * @see com.javaflow.application.common.Command
 * @since 1.0.0
 */
package com.javaflow.application.workflow;
