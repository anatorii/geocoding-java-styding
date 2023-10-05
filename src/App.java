import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.stream.Collectors;

public class App {
    static Dotenv dotenv = null;

    public static void main(String[] args) throws IOException {
        dotenv = Dotenv.configure()
                .directory("assets")
                .filename(".env")
                .load();

        String address;
        Scanner scanner = new Scanner(System.in);
        address = scanner.nextLine();
        while (!address.equals("q")) {
            System.out.println(address);
            searchAddress(address);
            address = scanner.nextLine();
        }
    }

    private static void searchAddress(String address) throws IOException {
        String apikey = "apikey=" + App.dotenv.get("API_KEY");
        String geocode = "&geocode=" + encodeValue(address);
        String uri = "https://geocode-maps.yandex.ru/1.x?" + apikey + "&lang=ru_RU&format=json" + geocode;

        System.out.println(uri);

        String response = getResponse(uri);

        JSONArray array;
        array = (new JSONObject(response))
                .getJSONObject("response")
                .getJSONObject("GeoObjectCollection")
                .getJSONArray("featureMember")
        ;

        JSONObject data;
        String result = "not found";
        for (Object obj : array) {
            data = ((JSONObject) obj).getJSONObject("GeoObject")
                    .getJSONObject("metaDataProperty")
                    .getJSONObject("GeocoderMetaData");
            if (data.getString("kind").equals("house")) {
                result = data.getJSONObject("Address").getString("formatted");
                break;
            }
        }
        System.out.println(result);
    }

    private static String getResponse(String uri) throws IOException {
        URL url = new URL(uri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(3000);
        connection.setRequestProperty("Content-Type", "application/json");

        InputStream responseStream = connection.getInputStream();
        InputStreamReader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(reader);

        return bufferedReader.lines().collect(Collectors.joining(""));
    }

    private static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }
}
