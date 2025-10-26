package com.javaflow.application.common;

/**
 * Base interface for all use cases in the application layer.
 * 
 * Use cases represent the application's business logic and orchestrate
 * the flow of data to and from the domain entities.
 * 
 * @param <I> Input type (Command/Query)
 * @param <O> Output type (Result/Response)
 */
public interface UseCase<I, O> {
    
    /**
     * Execute the use case with the given input
     * 
     * @param input The command or query input
     * @return The result of the use case execution
     */
    O execute(I input);
}