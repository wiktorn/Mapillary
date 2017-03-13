// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.mapillary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.openstreetmap.josm.Main;

/**
 * Each {@link MapillarySign} represents a traffic sign detected by the Mapillary's system.
 *
 * @author nokutu
 */
public class MapillarySign {

  private static final String[] COUNTRIES = {"au", "br", "ca", "eu", "us"};

  private static Map<String, HashMap<String, MapillarySign>> map = new HashMap<>();

  private final String fullName;
  private final String category;
  private final String type;
  private final String country;
  private final String variant;

  static {
    for (String country : COUNTRIES) {
      HashMap<String, MapillarySign> countryMap = new HashMap<>();
      String fileName = "/data/signs/" + country + ".cson";
      if (MapillarySign.class.getResource(fileName) != null) {
        try (
          BufferedReader br = new BufferedReader(new InputStreamReader(
            MapillarySign.class.getResourceAsStream(fileName), "UTF-8"
          ))
        ) {
          String line = "";
          while ((line = br.readLine()) != null) {
            if (!"".equals(line)) {
              String[] pair = line.replace("'", "").split(":");
              countryMap.put(pair[0].trim(), new MapillarySign(pair[1].trim()));
            }
          }
          map.put(country, countryMap);

          br.close();
        } catch (IOException e) {
          Main.error(e);
        }
      }
    }
  }

  /**
   * @param fullName the name of the sign
   * @throws NullPointerException if the name does not match the regex <code>.*--.*--.*</code> or if it is
   *   <code>null</code>.
   */
  public MapillarySign(String fullName) {
    this.fullName = fullName;
    String[] parts = fullName.split("--");
    category = parts[0];
    type = parts[1];
    country = parts[2];
    if (parts.length == 4) {
      variant = parts[3];
    } else {
      variant = null;
    }
  }

  public static MapillarySign getSign(String name, String country) {
    Map<String, MapillarySign> countryMap = map.get(country);
    if (countryMap == null) {
      Main.warn("Country does not exist");
      return null;
    }
    if (countryMap.containsKey(name)) {
      return countryMap.get(name);
    } else if (name.split("--").length >= 3) {
      if (countryMap.containsValue(new MapillarySign(name))) {
        Optional<MapillarySign> p = countryMap.values().stream().filter(sign -> sign.toString().equals(name)).findFirst();
        assert p.isPresent();
        return p.get();
      } else {
        return new MapillarySign(name);
      }
    } else {
      Main.warn("Sign '{0}' does not exist in the plugin database. Please contact the developer to add it.", name);
      return null;
    }
  }

  public String getFullName() {
    return fullName;
  }

  public String getCategory() {
    return category;
  }

  public String getType() {
    return type;
  }

  public String getCountry() {
    return country;
  }

  public String getVariant() {
    return variant;
  }

  @Override
  public String toString() {
    return fullName;
  }

  @Override
  public int hashCode() {
    return 31 + ((fullName == null) ? 0 : fullName.hashCode());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (fullName == null) {
      return ((MapillarySign) o).fullName == null;
    }
    return fullName.equals(((MapillarySign) o).fullName);
  }
}