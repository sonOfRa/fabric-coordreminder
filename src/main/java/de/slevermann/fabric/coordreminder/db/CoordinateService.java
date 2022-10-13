package de.slevermann.fabric.coordreminder.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CoordinateService {

    private static final String SELECT_COORDINATE_SQL = """
            SELECT * FROM COORDINATE
            WHERE NAME = ? AND OWNER = ?
            """;
    private static final String INSERT_COORDINATE_SQL = """
            INSERT INTO COORDINATE (NAME, REGISTRY, REGISTRY_VALUE, OWNER, X_COORD, Y_COORD, Z_COORD)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String DELETE_COORDINATE_SQL = """
            DELETE FROM COORDINATE
            WHERE NAME = ? AND OWNER = ?
            """;

    private static final String MOVE_COORDINATE_SQL = """
            UPDATE COORDINATE
            SET NAME = ?, OWNER = ?
            WHERE NAME = ? AND OWNER = ?
            """;
    private static final String LIST_COORDINATES_SQL = """
            SELECT * FROM COORDINATE
            WHERE OWNER = ?
            """;
    private static final String CLEAR_COORDINATES_SQL = """
            DELETE FROM COORDINATE
            WHERE OWNER = ?
            """;
    private static final String FIND_COORDINATES_SQL = """
            SELECT NAME FROM COORDINATE
            WHERE NAME LIKE ? AND OWNER = ?
            """;
    private final ConnectionPool connectionPool;

    public CoordinateService(final ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public DbCoordinate getCoordinate(final String name, final UUID owner) throws SQLException {
        try (final var conn = connectionPool.getConnection()) {
            try (final var stmt = conn.prepareStatement(SELECT_COORDINATE_SQL)) {
                stmt.setString(1, name);
                stmt.setString(2, owner.toString());
                try (final var rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    return mapResultSetRow(rs);
                }
            }
        }
    }

    public boolean createCoordinate(final DbCoordinate coordinate) throws SQLException {
        try (final var conn = connectionPool.getConnection()) {
            try (final var stmt = conn.prepareStatement(INSERT_COORDINATE_SQL)) {
                stmt.setString(1, coordinate.name());
                stmt.setString(2, coordinate.registry());
                stmt.setString(3, coordinate.registryValue());
                stmt.setString(4, coordinate.owner().toString());
                stmt.setDouble(5, coordinate.x());
                stmt.setDouble(6, coordinate.y());
                stmt.setDouble(7, coordinate.z());
                return stmt.executeUpdate() == 1;
            } catch (final SQLException ex) {
                if (ex.getSQLState().equals("23505") || ex.getSQLState().equals("23000")) {
                    return false;
                } else {
                    throw ex;
                }
            }
        }
    }

    public boolean deleteCoordinate(final String name, final UUID owner) throws SQLException {
        try (final var conn = connectionPool.getConnection()) {
            try (final var stmt = conn.prepareStatement(DELETE_COORDINATE_SQL)) {
                stmt.setString(1, name);
                stmt.setString(2, owner.toString());
                return stmt.executeUpdate() == 1;
            }
        }
    }

    public int moveCoordinate(final String name, final UUID owner, final String newName, final UUID newOwner)
            throws SQLException {
        try (final var conn = connectionPool.getConnection()) {
            try (final var stmt = conn.prepareStatement(MOVE_COORDINATE_SQL)) {
                stmt.setString(1, newName);
                stmt.setString(2, newOwner.toString());
                stmt.setString(3, name);
                stmt.setString(4, owner.toString());
                return stmt.executeUpdate();
            } catch (final SQLException ex) {
                if (ex.getSQLState().equals("23505") || ex.getSQLState().equals("23000")) {
                    return -1;
                } else {
                    throw ex;
                }
            }
        }
    }

    public List<DbCoordinate> listCoordinates(final UUID owner) throws SQLException {
        try (final var conn = connectionPool.getConnection()) {
            try (final var stmt = conn.prepareStatement(LIST_COORDINATES_SQL)) {
                stmt.setString(1, owner.toString());
                try (final var rs = stmt.executeQuery()) {
                    final var list = new ArrayList<DbCoordinate>();
                    while (rs.next()) {
                        list.add(mapResultSetRow(rs));
                    }
                    return list;
                }
            }
        }
    }

    public int clearCoordinates(final UUID owner) throws SQLException {
        try (final var conn = connectionPool.getConnection()) {
            try (final var stmt = conn.prepareStatement(CLEAR_COORDINATES_SQL)) {
                stmt.setString(1, owner.toString());
                return stmt.executeUpdate();
            }
        }
    }

    public List<String> findCoordinateNames(final String startsWith, final UUID owner) throws SQLException {
        try (final var conn = connectionPool.getConnection()) {
            try (final var stmt = conn.prepareStatement(FIND_COORDINATES_SQL)) {
                stmt.setString(1, startsWith + "%");
                stmt.setString(2, owner.toString());
                try (final var rs = stmt.executeQuery()) {
                    final var result = new ArrayList<String>();
                    while (rs.next()) {
                        result.add(rs.getString(1));
                    }
                    return result;
                }
            }
        }
    }

    private static DbCoordinate mapResultSetRow(final ResultSet rs) throws SQLException {
        long id = rs.getLong("ID");
        String coordName = rs.getString("NAME");
        String registry = rs.getString("REGISTRY");
        String registryValue = rs.getString("REGISTRY_VALUE");
        UUID coordOwner = UUID.fromString(rs.getString("OWNER"));
        double x = rs.getDouble("X_COORD");
        double y = rs.getDouble("Y_COORD");
        double z = rs.getDouble("Z_COORD");
        return new DbCoordinate(id, coordName, registry, registryValue, coordOwner, x, y, z);
    }
}
