package server;

import com.google.gson.*;

import java.io.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class Database {

    private Gson gson = new Gson();
    private final Map<Object, Object> mapa = new HashMap<>();
    private final Map<String, Object> response = new LinkedHashMap<>();
    private final String filepath = "src/main/java/server/data/db.json";
    private BufferedReader bufferedReader;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    Database() {
        try {
            this.gson = new GsonBuilder().setPrettyPrinting().create();
            // READS JSON FILE ON STARTUP
            bufferedReader = new BufferedReader(new FileReader(filepath));
            JsonObject jsonObject = gson.fromJson(bufferedReader, JsonObject.class);
            jsonObject.keySet().forEach(keyString -> {
                Object keyValue = jsonObject.get(keyString);
                mapa.put(keyString, keyValue);

            });
        } catch (Exception e) {
            e.fillInStackTrace();
        }

    }

    synchronized String set(ArrayList<String> key, Object value) {
        try {
            writeLock.lock();
            if (key.size() == 1) {
                JsonElement jsonValue = gson.toJsonTree(value);
                mapa.put(key.get(0), jsonValue);
                System.out.println(mapa.get(key.get(0)));
                response.put("response", "OK");
                String gsonString = gson.toJson(response);
                response.clear();
                addToJSON();
                return gsonString;
            } else {
                if (mapa.containsKey(key.get(0))) {
                    JsonElement jsonElement = gson.toJsonTree(mapa.get(key.get(0)));
                    JsonObject replacedJson = addedNestedObject(jsonElement, key, value);
                    mapa.replace(key.get(0), mapa.get(key.get(0)), replacedJson);
                    response.put("response", "OK");
                    String gsonString = gson.toJson(response);
                    response.clear();
                    addToJSON();
                    return gsonString;
                }
            }
        } catch (Exception e) {
            e.fillInStackTrace();
        } finally {
            writeLock.unlock();
        }

        response.put("response", "ERROR");
        response.put("reason", "TEMPORARY");
        String gsonString = gson.toJson(response);
        response.clear();
        return gsonString;
    }

    String get(ArrayList<String> key) {
        try {
            readLock.lock();
            if (key.size() == 1) {
                if (mapa.containsKey(key.get(0))) {
                    return validGetResponse(mapa.get(key.get(0)));
                }
            } else {
                JsonElement json = gson.toJsonTree(mapa.get(key.get(0)));
                JsonObject section = json.getAsJsonObject();
                int maxIndex = 1;
                if (key.size() != 2) {
                    for (int i = 1; i < key.size() - 1; i++) {
                        section = section.get(key.get(i)).getAsJsonObject();
                    }
                    maxIndex++;
                }
                if (section.get(key.get(maxIndex)) == null) {
                    return invalidGetResponse();
                } else {
                    return validGetResponse(section.get(key.get(maxIndex)));
                }
            }
            return invalidGetResponse();
        } finally {
            readLock.unlock();
        }
    }

    private String validGetResponse(Object value) {
        response.put("response", "OK");
        response.put("value", value);
        String gsonString = gson.toJson(response);
        response.clear();
        return gsonString;
    }

    private String invalidGetResponse() {
        response.put("response", "ERROR");
        response.put("reason", "No such key");
        String gsonString = gson.toJson(response);
        response.clear();
        return gsonString;
    }

    synchronized String delete(ArrayList<String> key) {
        try {
            writeLock.lock();
            if (key.size() == 1) {
                if (mapa.containsKey(key)) {
                    mapa.remove(key.get(0));
                    response.put("response", "OK");
                    String gsonString = gson.toJson(response);
                    response.clear();
                    addToJSON();
                    return gsonString;
                }
            } else {
                if (mapa.containsKey(key.get(0))) {
                    JsonElement jsonElement = gson.toJsonTree(mapa.get(key.get(0)));
                    JsonObject replacedJson = removeNestedObject(jsonElement, key);
                    mapa.replace(key.get(0), mapa.get(key.get(0)), replacedJson);
                    response.put("response", "OK");
                    String gsonString = gson.toJson(response);
                    response.clear();
                    addToJSON();
                    return gsonString;
                }
            }
            response.put("response", "ERROR");
            response.put("reason", "No such key");
            String gsonString = gson.toJson(response);
            response.clear();
            return gsonString;
        } finally {
            writeLock.unlock();
        }
    }

    void addToJSON() {
        // ADDING/REMOVING HASHMAP TO JSON FILE (goes through entire hashmap)
        try (FileOutputStream fos = new FileOutputStream(filepath);
             OutputStreamWriter osr = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
            gson.toJson(mapa, osr);
        } catch (Exception e) {
            e.fillInStackTrace();
        }

    }

    private JsonObject removeNestedObject(JsonElement jsonElement, ArrayList<String> key) {
        JsonObject fullJson = jsonElement.getAsJsonObject();
        JsonElement insideElement = jsonElement.getAsJsonObject().get(key.get(1));
        String insideElementDeeper = insideElement.getAsJsonObject().toString();
        fullJson.add(key.get(1), JsonNull.INSTANCE);

        for (int i = 2; i < key.size(); i++) {
            if (2 == key.size() - 1) {
                //final nest destination
                JsonObject finalJson = insideElement.getAsJsonObject();
                //removal of final nested key
                finalJson.remove(key.get(i));
                //get the nest that still exists and add post removal section or remove if no section left:
                fullJson.add(key.get(i - 1), finalJson);

            } else if (i < key.size() - 1) {
                //two nests together
                insideElement = JsonParser.parseString(insideElementDeeper);
                //final nest destination
                insideElementDeeper = insideElement.getAsJsonObject().get(key.get(i)).toString();
                //removal of final nested key
                insideElement.getAsJsonObject().add(key.get(i), JsonNull.INSTANCE);
                //full nest, but with null value
                fullJson.add(key.get(1), insideElement);
                //change null with values left or remove key:value completely if no value left
                JsonObject surrogate = JsonParser.parseString(insideElementDeeper).getAsJsonObject();
                surrogate.remove(key.get(i + 1));
                //insideElement points at fullJson
                insideElement.getAsJsonObject().add(key.get(i), surrogate);

            }
        }
        return fullJson;
    }

    private JsonObject addedNestedObject(JsonElement jsonElement, ArrayList<String> key, Object value) {
        JsonObject fullJson = jsonElement.getAsJsonObject();
        JsonElement insideElement = jsonElement.getAsJsonObject().get(key.get(1));
        String insideElementDeeper = insideElement.getAsJsonObject().toString();
        fullJson.add(key.get(1), JsonNull.INSTANCE);

        for (int i = 2; i < key.size(); i++) {
            if (2 == key.size() - 1) {
                //final nest destination
                JsonObject finalJson = insideElement.getAsJsonObject();
                //adding new value to final nested key
                finalJson.add(key.get(i), JsonParser.parseString(value.toString()));
                //get the nest that still exists and add post removal section or remove if no section left:
                fullJson.add(key.get(i - 1), finalJson);

            } else if (i < key.size() - 1) {
                System.out.println("llll");
                //two nests together
                insideElement = JsonParser.parseString(insideElementDeeper);
                //final nest destination
                insideElementDeeper = insideElement.getAsJsonObject().get(key.get(i)).toString();
                //nulling final nested key
                insideElement.getAsJsonObject().add(key.get(i), JsonNull.INSTANCE);
                //full nest, but with null value
                fullJson.add(key.get(1), insideElement);
                //change null with new value
                JsonObject surrogate = JsonParser.parseString(insideElementDeeper).getAsJsonObject();
                surrogate.add(key.get(i + 1), JsonParser.parseString(value.toString()));
                //insideElement points at fullJson
                insideElement.getAsJsonObject().add(key.get(i), surrogate);

            }
        }
        return fullJson;
    }
}

