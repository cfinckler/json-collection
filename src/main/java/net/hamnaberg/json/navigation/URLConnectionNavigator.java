package net.hamnaberg.json.navigation;

import net.hamnaberg.json.Collection;
import net.hamnaberg.json.Item;
import net.hamnaberg.json.Template;
import net.hamnaberg.json.parser.CollectionParser;
import net.hamnaberg.json.util.Optional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;

public class URLConnectionNavigator implements Navigator {

    public static final String ACCEPT = "application/vnd.collection+json,*/*;q=0.1";

    @Override
    public final Optional<Collection> follow(URI href) {
        HttpURLConnection connection = null;
        try {
            connection = getConnection(href);
            if (isSuccessful(connection.getResponseCode()) && isCollectionJSON(connection)) {
                InputStream stream = connection.getInputStream();
                if (stream != null) {
                    return Optional.some(new CollectionParser().parse(stream));
                }
            } else if (isRedirection(connection.getResponseCode())) {
                String location = connection.getHeaderField("Location");
                if (location != null) {
                    return follow(URI.create(location));
                }
            }
            else {
                String out = toString(connection.getErrorStream());
                throw new RuntimeException(out);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return Optional.none();
    }

    @Override
    public final void create(URI href, Template template) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public final void update(Item item) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private boolean isCollectionJSON(HttpURLConnection connection) {
        return connection.getHeaderField("Content-Type").equals("application/vnd.collection+json");
    }

    private boolean isSuccessful(int responseCode) {
        return responseCode >= 200 && responseCode < 300;
    }

    private boolean isRedirection(int responseCode) {
        return responseCode >= 300 && responseCode < 400;
    }

    private static String toString(InputStream is) {
        if (is == null) {
            return null;
        }
        byte[] buffer = new byte[2048];
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int length;
        try {
            while((length = is.read(buffer)) != -1) {
                os.write(buffer, 0, length);
            }
            return os.toString("UTF-8");
        } catch (IOException e) {
            return null;
        }
        finally {
            try {
                is.close();
            } catch (IOException ignore) {
            }
        }
    }

    protected HttpURLConnection getConnection(URI href) throws IOException {
        URLConnection conn = href.toURL().openConnection();
        conn.addRequestProperty("Accept", ACCEPT);
        return (HttpURLConnection) conn;
    }
}
