package org.felixrilling.musicbrainzenricher.api.musicbrainz;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Consumer;

@Service
public class MusicbrainzAutoQueryService {

    private final DataSource dataSource;

    MusicbrainzAutoQueryService(@Qualifier("musicbrainzLocalDb") DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void autoQueryReleasesWithRelationships(@NotNull Consumer<String> mbidConsumer) {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection
                .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            try (ResultSet rs = statement
                    .executeQuery("SELECT r.gid FROM release r WHERE r.id IN (SELECT lru.entity0 FROM l_release_url lru)")) {
                // gid == mbid
                pipeToCallback(rs, "gid", mbidConsumer);
            }
        } catch (SQLException e) {
            throw new QueryException("Error running query.", e);
        }
    }

    public void autoQueryReleaseGroupsWithRelationships(@NotNull Consumer<String> mbidConsumer) {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection
                .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            try (ResultSet rs = statement
                    .executeQuery("SELECT rg.gid FROM release_group rg WHERE rg.id IN (SELECT lrgu.entity0 FROM l_release_group_url lrgu)")) {
                // gid == mbid
                pipeToCallback(rs, "gid", mbidConsumer);
            }
        } catch (SQLException e) {
            throw new QueryException("Error running query.", e);
        }
    }


    private void pipeToCallback(@NotNull ResultSet rs, @NotNull String columnName, @NotNull Consumer<String> consumer) throws SQLException {
        while (rs.next()) {
            consumer.accept(rs.getString(columnName));
        }
    }

}
