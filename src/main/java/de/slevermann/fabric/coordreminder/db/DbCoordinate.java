package de.slevermann.fabric.coordreminder.db;

import java.util.UUID;

public record DbCoordinate(Long id, String name, String registry, String registryValue, UUID owner,
                           double x, double y, double z) {

    public DbCoordinate(final String name, final String registry, final String registryValue, final UUID owner,
                        final double x, final double y, final double z) {
        this(null, name, registry, registryValue, owner, x, y, z);
    }
}
