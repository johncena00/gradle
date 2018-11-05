/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.tasks.compile.processing;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.gradle.internal.operations.BuildOperationContext;
import org.gradle.internal.operations.BuildOperationDescriptor;
import org.gradle.internal.operations.BuildOperationExecutor;
import org.gradle.internal.operations.CallableBuildOperation;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.util.Set;

public class BuildOperationReportingProcessor extends DelegatingProcessor {

    private final BuildOperationExecutor executor;
    private final String processorClassName;

    public BuildOperationReportingProcessor(BuildOperationExecutor executor, Processor processor) {
        super(processor);
        this.executor = executor;
        this.processorClassName = determineClassName(processor);
    }

    private String determineClassName(Processor processor) {
        if (processor instanceof DelegatingProcessor) {
            return determineClassName(((DelegatingProcessor) processor).getDelegate());
        }
        return processor.getClass().getName();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        return executor.call(new CallableBuildOperation<Boolean>() {

            @Override
            public BuildOperationDescriptor.Builder description() {
                Set<String> annotationNames = ImmutableSet.copyOf(Iterables.transform(annotations, new Function<TypeElement, String>() {
                    @Override
                    public String apply(TypeElement input) {
                        return input.getQualifiedName().toString();
                    }
                }));
                return BuildOperationDescriptor.displayName("Process annotations with " + processorClassName)
                    .details(new Details(processorClassName, annotationNames));
            }

            @Override
            public Boolean call(BuildOperationContext context) {
                boolean claimed = BuildOperationReportingProcessor.super.process(annotations, roundEnv);
                context.setResult(new Result(claimed));
                return claimed;
            }
        });
    }

    private static class Details implements ProcessAnnotationsBuildOperationType.Details {
        private final String processorClassName;
        private final Set<String> annotationNames;

        Details(String processorClassName, Set<String> annotationNames) {
            this.processorClassName = processorClassName;
            this.annotationNames = annotationNames;
        }

        @Override
        public String getProcessorClassName() {
            return processorClassName;
        }

        @Override
        public Set<String> getAnnotationNames() {
            return annotationNames;
        }
    }

    private static class Result implements ProcessAnnotationsBuildOperationType.Result {
        private final boolean claimed;

        Result(boolean claimed) {
            this.claimed = claimed;
        }

        @Override
        public boolean isClaimed() {
            return claimed;
        }
    }

}
