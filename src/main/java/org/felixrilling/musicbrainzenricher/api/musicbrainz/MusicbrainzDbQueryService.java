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
                try (ResultSet resultSet = statement
                        .executeQuery("SELECT (r.gid) FROM release r WHERE r.id IN (SELECT lru.entity0 FROM l_release_url lru)")) {
                    // gid == mbid
                    pipeToCallback(resultSet, "gid", consumer);
                }
            }
        } catch (SQLException e) {
            throw new QueryException("Error running query.", e);
        }
    }

    public void queryReleaseGroupsWithRelationships(@NotNull Consumer<String> consumer) throws QueryException {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
                // Only check release groups that have one or more entries in the release group -> url table
                try (ResultSet resultSet = statement
                        .executeQuery("SELECT (rg.gid) FROM release_group rg WHERE rg.id IN (SELECT lrgu.entity0 FROM l_release_group_url lrgu)")) {
                    // gid == mbid
                    pipeToCallback(resultSet, "gid", consumer);
                }
            }
        } catch (SQLException e) {
            throw new QueryException("Error running query.", e);
        }
    }

    private void pipeToCallback(@NotNull ResultSet resultSet, @NotNull String columnName, @NotNull Consumer<String> consumer) throws SQLException {
        while (resultSet.next()) {
            consumer.accept(resultSet.getString(columnName));
        }
    }

}
