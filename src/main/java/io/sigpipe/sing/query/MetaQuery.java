package io.sigpipe.sing.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.sigpipe.sing.dataset.feature.Feature;
import io.sigpipe.sing.dataset.feature.FeatureType;
import io.sigpipe.sing.graph.DataContainer;
import io.sigpipe.sing.graph.Vertex;
import io.sigpipe.sing.serialization.SerializationException;
import io.sigpipe.sing.serialization.SerializationInputStream;
import io.sigpipe.sing.serialization.SerializationOutputStream;

public class MetaQuery extends Query {

    private Map<String, List<Expression>> expressions = new HashMap<>();

    public MetaQuery() {

    }

    public void addExpression(Expression e) {
        String name = e.getOperand().getName();
        List<Expression> expList = expressions.get(name);
        if (expList == null) {
            expList = new ArrayList<>();
            expressions.put(name, expList);
        }
        expList.add(e);
    }

    public void execute(Vertex root, SerializationOutputStream out)
    throws IOException, QueryException {
        this.query(root, out);
    }

    private void query(Vertex vertex, SerializationOutputStream out)
    throws IOException, QueryException {
        DataContainer container = vertex.getData();
        if (container != null) {
            //System.out.println("Serializing: " + vertex.toString());
            container.serialize(out);
        }

        List<Expression> expList = expressions.get(vertex.getLabel().getName());
        if (expList != null) {
            Set<Vertex> matches = evaluate(vertex, expList);
            for (Vertex match : matches) {
                if (match == null) {
                    continue;
                }

                if (match.getLabel().getType() == FeatureType.NULL) {
                    continue;
                }

                query(match, out);
            }
        } else {
            /* No expression operates on this vertex. Consider all children. */
            for (Vertex neighbor : vertex.getAllNeighbors()) {
                query(neighbor, out);
            }
        }
    }

    private Set<Vertex> evaluate(Vertex vertex, List<Expression> expressions)
    throws QueryException {
        Set<Vertex> matches = new HashSet<>(vertex.numNeighbors(), 1.0f);
        for (Expression expression : expressions) {

            Operator operator = expression.getOperator();
            Feature operand = expression.getOperand();

            switch (operator) {
                case EQUAL: {
                    matches.add(vertex.getNeighbor(operand));
                    break;
                }

                case NOTEQUAL: {
                    boolean exists = matches.contains(operand);
                    matches.addAll(vertex.getAllNeighbors());
                    if (exists == false) {
                        /* If the operand (not equal value) wasn't already added
                         * by another expression, we can safely remove it now.
                         * In other words, if another expression includes the
                         * value excluded by this expression, the user has
                         * effectively requested the entire neighbor set. */
                        matches.remove(operand);
                    }
                    break;
                }

                case LESS: {
                    matches.addAll(
                            vertex.getNeighborsLessThan(operand, false)
                            .values());
                    break;
                }

                case LESSEQUAL: {
                    matches.addAll(
                            vertex.getNeighborsLessThan(operand, true)
                            .values());
                    break;
                }

                case GREATER: {
                    matches.addAll(
                            vertex.getNeighborsGreaterThan(operand, false)
                            .values());
                    break;
                }

                case GREATEREQUAL: {
                    matches.addAll(
                            vertex.getNeighborsGreaterThan(operand, true)
                            .values());
                    break;
                }

                case RANGE_INC: {
                    Feature secondOperand = expression.getSecondOperand();
                    matches.addAll(vertex.getNeighborsInRange(
                                operand, true,
                                secondOperand, true)
                            .values());
                    break;
                }

                case RANGE_EXC: {
                    Feature secondOperand = expression.getSecondOperand();
                    matches.addAll(vertex.getNeighborsInRange(
                                operand, false,
                                secondOperand, false)
                            .values());
                    break;
                }

                case RANGE_INC_EXC: {
                    Feature secondOperand = expression.getSecondOperand();
                    matches.addAll(vertex.getNeighborsInRange(
                                operand, true,
                                secondOperand, false)
                            .values());
                    break;
                }

                case RANGE_EXC_INC: {
                    Feature secondOperand = expression.getSecondOperand();
                    matches.addAll(vertex.getNeighborsInRange(
                                operand, false,
                                secondOperand, true)
                            .values());
                    break;
                }

                default:
                    throw new QueryException("Unknown operator: " + operator);
            }
        }

        return matches;
    }

    @Deserialize
    public MetaQuery(SerializationInputStream in)
    throws IOException, SerializationException {
        int size = in.readInt();
        expressions = new HashMap<>(size);
        for (int i = 0; i < size; ++i) {
            int listSize = in.readInt();
            List<Expression> expList = new ArrayList<>(listSize);
            for (int j = 0; j < listSize; ++j) {
                Expression exp = new Expression(in);
                expList.add(exp);
            }
            String featureName = expList.get(0).getOperand().getName();
            expressions.put(featureName, expList);
        }
    }

    @Override
    public void serialize(SerializationOutputStream out)
    throws IOException {
        out.writeInt(expressions.size());
        for (List<Expression> expList : expressions.values()) {
            out.writeInt(expList.size());
            for (Expression expression : expList) {
                expression.serialize(out);
            }
        }
    }

}

