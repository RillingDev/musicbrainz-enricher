package org.felixrilling.musicbrainzenricher.api.musicbrainz;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// Not a 'real' repository because we only look up primitive values and not entities
@Component
public class GenreRepository {

    private final DataSource dataSource;

    GenreRepository(@Qualifier("musicbrainzLocalDb") DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<String> findGenreNames() {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            try (ResultSet rs = statement
                    .executeQuery("SELECT g.name FROM genre g ORDER BY g.name")) {
                List<String> genres = new ArrayList<>();
                while (rs.next()) {
                    genres.add(rs.getString("name"));
                }
                return Collections.unmodifiableList(genres);
            }
        } catch (SQLException e) {
            throw new QueryException("Error running query.", e);
        }
    }

    public Optional<String> findGenreNameByMbid(String mbid) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT g.name FROM genre g WHERE g.gid::TEXT LIKE ?")) {
                statement.setString(1, mbid);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(rs.getString("name"));
                    } else {
                        return Optional.empty();
                    }
                }
            }
        } catch (SQLException e) {
            throw new QueryException("Error running query.", e);
        }
    }

}
