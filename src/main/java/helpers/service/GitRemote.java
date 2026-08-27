package helpers.service;

import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public record GitRemote(String url, String login, String password) {

    public static final String ANONYMOUS = "anonymous";

    public GitRemote {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("A git remote needs a URL");
        }
        if (login == null || password == null) {
            throw new IllegalArgumentException(
                    "JGit's UsernamePasswordCredentialsProvider rejects null credentials, remote: " + url);
        }
    }

    public static GitRemote anonymous(String url) {
        return new GitRemote(url, ANONYMOUS, ANONYMOUS);
    }

    public UsernamePasswordCredentialsProvider credentials() {
        return new UsernamePasswordCredentialsProvider(login, password);
    }

    @Override
    public String toString() {
        return "GitRemote[url=" + url + ", login=" + login + ", password=***]";
    }
}
