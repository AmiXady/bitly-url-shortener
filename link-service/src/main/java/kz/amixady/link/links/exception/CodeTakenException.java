package kz.amixady.link.links.exception;

public class CodeTakenException extends RuntimeException {

    public CodeTakenException(String code) {
        super("Code already taken " + code);
    }
}
