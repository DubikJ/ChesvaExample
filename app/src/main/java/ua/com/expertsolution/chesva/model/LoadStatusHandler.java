package ua.com.expertsolution.chesva.model;

public class LoadStatusHandler {

    private int inSearchOf;
    private int found;
    private int confirm;
    private int excessive;
    private int missing;
    private int created;

    public LoadStatusHandler(int inSearchOf, int found, int confirm, int excessive, int missing, int created) {
        this.inSearchOf = inSearchOf;
        this.found = found;
        this.confirm = confirm;
        this.excessive = excessive;
        this.missing = missing;
        this.created = created;
    }

    public int getInSearchOf() {
        return inSearchOf;
    }

    public void setInSearchOf(int inSearchOf) {
        this.inSearchOf = inSearchOf;
    }

    public int getFound() {
        return found;
    }

    public void setFound(int found) {
        this.found = found;
    }

    public int getConfirm() {
        return confirm;
    }

    public void setConfirm(int confirm) {
        this.confirm = confirm;
    }

    public int getExcessive() {
        return excessive;
    }

    public void setExcessive(int excessive) {
        this.excessive = excessive;
    }

    public int getMissing() {
        return missing;
    }

    public void setMissing(int missing) {
        this.missing = missing;
    }

    public int getCreated() {
        return created;
    }

    public void setCreated(int created) {
        this.created = created;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private int inSearchOf;
        private int found;
        private int confirm;
        private int excessive;
        private int missing;
        private int created;

        public Builder inSearchOf(int inSearchOf) {
            this.inSearchOf = inSearchOf;
            return this;
        }

        public Builder found(int found) {
            this.found = found;
            return this;
        }

        public Builder confirm(int confirm) {
            this.confirm = confirm;
            return this;
        }

        public Builder excessive(int excessive) {
            this.excessive = excessive;
            return this;
        }

        public Builder missing(int missing) {
            this.missing = missing;
            return this;
        }

        public Builder created(int created) {
            this.created = created;
            return this;
        }

        public LoadStatusHandler build() {
            return new LoadStatusHandler(inSearchOf, found, confirm, excessive, missing, created);
        }

    }
}
