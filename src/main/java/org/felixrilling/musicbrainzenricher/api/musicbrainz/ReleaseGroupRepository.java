package org.felixrilling.musicbrainzenricher.api.musicbrainz;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Not a 'real' repository because we only look up primitive values and not entities
@Component
class ReleaseGroupRepository {
    private final DataSource dataSource;

    ReleaseGroupRepository(@Qualifier("musicbrainzLocalDb") DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public long countReleaseGroupsWhereRelationshipsExist() throws SQLException {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            try (ResultSet rs = statement
                    .executeQuery("SELECT COUNT(*) FROM release_group rg WHERE rg.id IN (SELECT lrgu.entity0 FROM l_release_group_url lrgu)")) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public List<String> findReleaseGroupsMbidWhereRelationshipsExist(long offset, int limit) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection
                    .prepareStatement("SELECT rg.gid FROM release_group rg WHERE rg.id IN (SELECT lrgu.entity0 FROM l_release_group_url lrgu) OFFSET ? LIMIT ?")) {
                statement.setLong(1, offset);
                statement.setInt(2, limit);
                try (ResultSet rs = statement
                        .executeQuery()) {
                    List<String> mbids = new ArrayList<>(limit);
                    while (rs.next()) {
                        mbids.add(rs.getString("gid"));
                    }
                    return Collections.unmodifiableList(mbids);
                }
            }
        }
    }
}
