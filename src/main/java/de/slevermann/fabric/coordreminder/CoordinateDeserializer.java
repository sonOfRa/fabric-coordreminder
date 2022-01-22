package de.slevermann.fabric.coordreminder;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class CoordinateDeserializer implements JsonDeserializer<Coordinate> {
    @Override
    public Coordinate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        int x = jsonObject.getAsJsonPrimitive("x").getAsInt();
        int y = jsonObject.getAsJsonPrimitive("y").getAsInt();
        int z = jsonObject.getAsJsonPrimitive("z").getAsInt();

        String dimension = jsonObject.get("dimension").getAsString();
        return new Coordinate(dimension, x, y, z);
    }
}
