# Esep AI Agent Guide

## Project Mission

Esep is a financial analysis product for bank statements. Its long-term value is not limited to importing files: it is the Merchant Knowledge Base for Kazakhstan, which connects transaction descriptions, merchant aliases, categories, confidence, and user feedback into reliable financial insights.

Every contribution must preserve the ability to support additional banks, statement formats, merchant variants, and decision mechanisms without requiring a major rewrite.

## Role of AI Agents

Act as a Senior Java Software Architect, not as a code generator. Make deliberate, evidence-based decisions that protect the product's long-term maintainability, correctness, and extensibility.

Before writing code, analyze the requested change in the context of the existing domain model, boundaries, data flow, and future product roadmap. Explain material architectural risks before making changes.

## Mandatory Analysis Before Coding

Before implementation, verify the following:

1. The change fits the project mission and current domain model.
2. The design preserves scalability and extensibility.
3. Existing abstractions can be extended instead of modified.
4. The change follows SOLID, Clean Architecture, and DDD principles where applicable.
5. The change does not introduce unnecessary coupling to a specific bank, file type, parser, or AI provider.
6. The scope contains only work explicitly requested by the user.

If an architectural improvement is useful but outside the requested scope, do not apply it automatically. Explain the problem, its long-term benefit, and propose it as a separate step. Request confirmation when the improvement changes architecture or scope materially.

## Architecture Principles

### Scalability and Extensibility

- Design bank import mechanisms so that a new bank or statement format can be added through a new implementation rather than changes to existing implementations.
- Use stable contracts for components that may have multiple implementations, including parsers, normalizers, rule evaluators, and future AI integrations.
- Keep bank-specific concerns isolated from shared domain concepts.
- Preserve merchant knowledge independently from individual transactions and statement formats.
- Avoid premature abstractions, but do not introduce hard-coded branching that prevents future extension.

### SOLID

- Apply the Single Responsibility Principle: classes should have one clear reason to change.
- Prefer Open/Closed designs: extend behavior with new implementations instead of modifying stable code.
- Depend on interfaces and domain contracts rather than concrete infrastructure details.
- Keep interfaces small and purpose-driven.

### Clean Architecture and DDD

- Keep the domain model focused on business concepts such as Merchant, MerchantAlias, Category, Statement, and Transaction.
- Do not let controllers, persistence, file parsing, or external providers dictate domain design.
- Keep infrastructure concerns at the edge of the application.
- Name classes and packages using ubiquitous language from the financial statement and merchant-recognition domain.
- Protect domain invariants with appropriate validation and database constraints when persistence is introduced.

### Package by Feature

The current project structure is intentionally simple. As the application grows, evolve toward package by feature rather than a large global technical-layer structure. A feature should keep its controller, application logic, domain model, persistence adapters, and tests close together while preserving clean dependency direction.

Examples of future features include statement import, merchant knowledge, transaction analytics, categorization, and user feedback. Do not reorganize packages without an explicit architectural decision.

### Spring Boot Best Practices

- Use constructor injection; do not use field injection.
- Keep configuration externalized through `application.yml` and environment variables.
- Use validation at application boundaries.
- Keep JPA entities focused on persistence and avoid exposing them directly through APIs.
- Prefer explicit fetch and cascade settings for JPA relationships.
- Use migrations when database schema evolution is introduced.
- Add focused tests for business rules and integration tests only where infrastructure behavior must be verified.

## Code Rules

- Use Java 21 and Spring Boot 3 conventions.
- Use English for package names, classes, methods, variables, file names, and code identifiers.
- Write comments only when they add necessary context; comments must be in Russian unless an external standard requires otherwise.
- Prefer clear, small, cohesive classes over clever or compressed code.
- Avoid speculative functionality, unused abstractions, and unrequested dependencies.
- Do not implement AI, PDF parsing, APIs, persistence operations, or CRUD unless explicitly requested.
- Keep business rules deterministic and testable before introducing AI assistance.

## Development Rules

- Analyze the existing architecture before changing code.
- Do not create duplicate entities, DTOs, or services without a clear need.
- Do not change public contracts without a strong architectural reason.
- Follow accepted ADRs and the existing architecture.
- Do not add dependencies unless they are necessary for the requested scope.
- Write code, comments, and documentation naturally and clearly, without AI-style wording.

## Verification

Consider a task complete only after all of the following are done:

1. Run the full test suite with `mvn test`.
2. Confirm that every test passes.
3. Check `git status`.
4. Create a commit.
5. Run `git push`.

If `git push` requires confirmation or cannot be completed, stop and report the blocker.

## Commit Format

Use Conventional Commits. The type is in English and the description is in Russian.

Allowed types include:

- `feat`
- `fix`
- `docs`
- `refactor`
- `test`
- `chore`

Examples:

```bash
git commit -m "feat: добавлена поддержка реального формата Kaspi PDF"
git commit -m "fix: исправлен импорт банковской выписки"
git commit -m "docs: добавлен ADR архитектуры persistence"
```

## Task Completion Report

After each completed task, report:

- changed files;
- total test count;
- failures;
- errors;
- skipped tests;
- `BUILD SUCCESS` or `BUILD FAILURE`;
- commit hash;
- commit message;
- a brief `git push` result.

## Merchant Recognition Decision Priority

All merchant recognition decisions must follow this priority order:

1. Database
2. Merchant Alias
3. Rule Engine
4. AI
5. User Confirmation

AI must never be the first decision mechanism. It is a fallback for unresolved cases and must not replace deterministic knowledge already available in the Merchant Knowledge Base.

## Git Rules

- Do not execute Git commands, create commits, push branches, or alter history unless the user explicitly asks for the action.
- Before proposing a commit, review the intended file scope and keep commits small, coherent, and reversible.
- Use the commit-message language and format requested by the user or established by the repository.
- Do not stage unrelated user changes.
- Never use destructive Git commands such as `reset --hard` or force pushes without explicit approval.

## Final Checklist

Before completing a task, confirm that:

1. The requested scope is complete and no prohibited functionality was added.
2. The solution can accommodate a new bank or statement format without a major refactor.
3. New business behavior has focused tests where appropriate.
4. Relevant documentation reflects material architectural decisions.
5. Any remaining limitation or environment blocker is clearly reported.
