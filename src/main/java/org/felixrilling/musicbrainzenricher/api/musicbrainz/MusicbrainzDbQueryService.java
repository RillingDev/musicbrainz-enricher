package org.felixrilling.musicbrainzenricher.api.musicbrainz;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Consumer;

@Service
@Profile("musicbrainzLocalDb")
public class MusicbrainzDbQueryService {

    private final DataSource dataSource;

    public MusicbrainzDbQueryService(@Qualifier("musicbrainzLocalDb") DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void queryReleasesWithRelationships(@NotNull Consumer<String> consumer) throws QueryException {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
                // Only check releases that have one or more entries in the release -> url table
                ResultSet resultSet = statement
                        .executeQuery("SELECT (r.gid) FROM release r WHERE r.id IN (SELECT lru.entity0 FROM l_release_url lru)");
                while (resultSet.next()) {
                    String mbid = resultSet.getString(1); // gid == mbid
                    consumer.accept(mbid);
                }
            }
        } catch (SQLException e) {
            throw new QueryException("Error running query.", e);
        }
    }

}
