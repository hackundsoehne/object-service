package edu.ipd.kit.crowdcontrol.proto.json;

import com.google.gson.*;
import edu.ipd.kit.crowdcontrol.proto.controller.BadRequestException;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * from: <a href=http://stackoverflow.com/questions/21626690/gson-optional-and-required-fields>stackoverflow</a>
 * @author LeanderK
 * @version 1.0
 */
public class JSONRequestChecker<T> implements JsonDeserializer<T> {

    /**
     * Gson invokes this call-back method during deserialization when it encounters a field of the
     * specified type.
     * <p>In the implementation of this call-back method, you should consider invoking
     * {@link JsonDeserializationContext#deserialize(JsonElement, Type)} method to create objects
     * for any non-trivial field of the returned object. However, you should never invoke it on the
     * the same type passing {@code json} since that will cause an infinite loop (Gson will call your
     * call-back method again).
     *
     * @param json    The Json data being deserialized
     * @param typeOfT The type of the Object to deserialize to
     * @param context
     * @return a deserialized object of the specified type typeOfT which is a subclass of {@code T}
     * @throws JsonParseException if json is not in the expected format of {@code typeofT}
     */
    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        T pojo = new Gson().fromJson(json, typeOfT);

        Field[] fields = pojo.getClass().getDeclaredFields();
        for (Field f : fields)
        {
            if (f.getAnnotation(JSONRequired.class) != null)
            {
                try
                {
                    f.setAccessible(true);
                    if (f.get(pojo) == null)
                    {
                        throw new BadRequestException(f.getName() + " is required");
                    }
                }
                catch (IllegalArgumentException | IllegalAccessException ex)
                {
                    Logger.getLogger(JSONRequestChecker.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return pojo;
    }
}
