import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherFetcher {
    private static final String API_KEY = "402b41e46b9b0067ac08a877b39da040"; // Replace with your API key

    // Fetch weather data for a list of city IDs
    public static JSONArray getWeatherDataByCityIDs(List<String> cityIDs) throws Exception {
        String ids = String.join(",", cityIDs);
        String urlString = "http://api.openweathermap.org/data/2.5/group?id="
                + ids + "&appid=" + API_KEY + "&units=metric";
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        // Check for successful response code or throw error
        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(
                (conn.getInputStream())));

        StringBuilder sb = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
            sb.append(output);
        }

        conn.disconnect();
        JSONObject response = new JSONObject(sb.toString());
        return response.getJSONArray("list");
    }
}
