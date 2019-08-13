package krs.ar.outar;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiInterface {

    String JSONURL = "https://api.myjson.com/bins/vnkq3";

    @GET("json_parsing.php")
    Call<String> getString();

}