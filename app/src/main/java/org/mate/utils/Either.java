package org.mate.utils;

import java.util.NoSuchElementException;

public class Either<L, R> {
    private final L left;
    private final R right;

    private Either(L left, R right) {
        if ((left == null && right == null) || (left != null && right != null)) {
            throw new IllegalArgumentException("Exactly one of the two options must be null!");
        }
        this.left = left;
        this.right = right;
    }

    public static <L, R> Either<L, R> left(L left) {
        return new Either<>(left, null);
    }

    public static <L, R> Either<L, R> right(R right) {
        return new Either<>(null, right);
    }

    public boolean hasLeft() {
        return left != null;
    }

    public boolean hasRight() {
        return right != null;
    }

    public L getLeft() {
        if (!hasLeft()) {
            throw new NoSuchElementException();
        }
        return left;
    }

    public R getRight() {
        if (!hasRight()) {
            throw new NoSuchElementException();
        }
        return right;
    }
}
