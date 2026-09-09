package com.fiapon.history.config;

import com.fiapon.history.exceptions.DuplicateHistoryFoundException;
import com.fiapon.history.exceptions.HistoryNotFoundException;
import com.fiapon.history.exceptions.InvalidHistoryDataException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

// Without this, Spring GraphQL's default handling turns every business exception thrown by
// HistoryService (not found, duplicate, missing fields) into an opaque INTERNAL_ERROR with no
// message, indistinguishable from an actual server failure. This maps them to proper error
// types and keeps the real message.
@Component
public class GraphQlExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment environment) {
        if (ex instanceof HistoryNotFoundException) {
            return buildError(ex, ErrorType.NOT_FOUND, environment);
        }
        if (ex instanceof DuplicateHistoryFoundException || ex instanceof InvalidHistoryDataException) {
            return buildError(ex, ErrorType.BAD_REQUEST, environment);
        }
        return null;
    }

    private GraphQLError buildError(Throwable ex, ErrorType errorType, DataFetchingEnvironment environment) {
        return GraphqlErrorBuilder.newError(environment)
                .errorType(errorType)
                .message(ex.getMessage())
                .build();
    }
}
