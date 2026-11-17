package kz.amixady.link.links.dto.response;

public record ShortenResult(LinkResponse link, Outcome outcome) {
    public enum Outcome {
        CREATED,
        EXISTING
    }

    public static ShortenResult created(LinkResponse link) {
        return new ShortenResult(link, Outcome.CREATED);
    }

    public static ShortenResult existing(LinkResponse link) {
        return new ShortenResult(link, Outcome.EXISTING);
    }

    public boolean isCreated(){
        return this.outcome == Outcome.CREATED;
    }

}