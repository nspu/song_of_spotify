package com.nspu.songofspotify.controllers;

import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.util.Log;

import com.nspu.songofspotify.models.CustomTrack;
import com.nspu.songofspotify.utils.ArrayToString;
import com.nspu.songofspotify.views.dialog.WaitingDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by nspu on 04/03/18.
 * <p>
 * The SpotifyRestApi class call spotify api (REST).
 * It's just an exemple to use AsyncTask and .
 * The best way is to use library like Retrofit to have a consise code.
 */
public class SpotifyRestApi {
    private static final String API_URI = "https://api.spotify.com/v1";

    private static SpotifyRestApi ourInstance;
    private final String mApiToken;
    private final MutableLiveData<List<CustomTrack>> mObservableCurrentPlayList = new MutableLiveData<>();

    public static SpotifyRestApi getInstance() {
        return ourInstance;
    }

    /**
     * Initialise the singleton only the first time this method is called.
     *
     * @param token api token for spotify authorization
     * @throws Throwable Throw if token is null or empty
     */
    public static void init(String token) throws Throwable {
        if (token == null || token.isEmpty())
            throw new Throwable("token not declared. Impossible to instantiate SpotifyContainer");

        if (ourInstance == null) {
            ourInstance = new SpotifyRestApi(token);
        }
    }

    private SpotifyRestApi(String apiToken) {
        mApiToken = apiToken;
    }

    public MutableLiveData<List<CustomTrack>> getObservableCurrentPlayList() {
        return mObservableCurrentPlayList;
    }

    public void getCurrentPlaylist() {
        new GetPlaylistAsync(mApiToken, mObservableCurrentPlayList).execute();
    }

    /**
     * GetPlaylistAsync call spotify api to get the playlist.
     */
    private static class GetPlaylistAsync extends AsyncTask<Void, Void, List<CustomTrack>> {
        final String TAG = "GetPlaylistAsync";

        String mApiToken;
        MutableLiveData<List<CustomTrack>> mObservableCurrentPlayList;


        public GetPlaylistAsync(String token, MutableLiveData<List<CustomTrack>> mObservableCurrentPlayList) {
            this.mApiToken = token;
            this.mObservableCurrentPlayList = mObservableCurrentPlayList;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            WaitingDialog.getInstance().display(TAG);
        }

        @Override
        protected List<CustomTrack> doInBackground(Void... voids) {
            List<CustomTrack> customTrackList = null;
            JSONObject json;

            try {
                HttpsURLConnection co = createRequest(
                        String.format("/users/%s/playlists/%s",
                                SpotifyContainer.USER_ID,
                                SpotifyContainer.PLAYLIST_ID));

                if (co.getResponseCode() == 200) {

                    json = responseToJson(co);

                    customTrackList = fillListWithJson(json);

                } else {
                    //Display error
                    Log.d("SpotifyRestApi", "Error response : " + co.getResponseCode());
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return customTrackList;
        }

        @Override
        protected void onPostExecute(List<CustomTrack> customTrackList) {
            super.onPostExecute(customTrackList);
            mObservableCurrentPlayList.setValue(customTrackList);
            try {
                WaitingDialog.getInstance().hide(TAG);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }

        /**
         * @param path path to the endpoint
         * @return HttpsURLConnection ready to be called
         * @throws IOException
         */
        private HttpsURLConnection createRequest(String path) throws IOException {
            URL apiSpotifyEndpoint = new URL(
                    String.format("%s%s",
                            API_URI, path));
            HttpsURLConnection co = (HttpsURLConnection) apiSpotifyEndpoint.openConnection();
            co.setRequestProperty("Authorization", "Bearer " + mApiToken);

            return co;
        }

        /**
         * Parse the response to a json
         *
         * @param co HttpsURLConnection already configurate to be called
         * @return Json of the playlist
         * @throws IOException
         * @throws JSONException
         */
        private JSONObject responseToJson(HttpsURLConnection co) throws IOException, JSONException {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(co.getInputStream()));

            String inputLine;
            StringBuilder buffer = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                buffer.append(inputLine);
            }

            return new JSONObject(buffer.toString());

        }

        /**
         * Create a list of custom track from the Json
         *
         * @param json contains the list of track in Json format
         * @return List<CustomTrack>
         * @throws JSONException
         */
        private List<CustomTrack> fillListWithJson(JSONObject json) throws JSONException {
            List<CustomTrack> customTrackList = new ArrayList<>();
            JSONArray ja = json.getJSONObject("tracks").getJSONArray("items");
            for (int i = 0; i < ja.length(); i++) {
                customTrackList.add(getCustomTrack(ja.getJSONObject(i)));
            }
            return customTrackList;
        }

        /**
         * Create a customTrack from the Json
         *
         * @param jsonObject contains the track in Json format
         * @return CustomTrack
         * @throws JSONException
         */
        private CustomTrack getCustomTrack(JSONObject jsonObject) throws JSONException {
            JSONObject jsonTrack = jsonObject.getJSONObject("track");
            String name = jsonTrack.getString("name");
            String uri = jsonTrack.getString("uri");
            String albumName = jsonTrack.getJSONObject("album").getString("name");
            List<Image> covers = getCovers(jsonTrack.getJSONObject("album").getJSONArray("images"));
            String artists = getArtits(jsonTrack.getJSONArray("artists"));
            return new CustomTrack(name, artists, covers, uri, albumName);
        }

        /**
         * Create covers from the Json
         *
         * @param jsonArray contains the covers in Json format
         * @return List<Image>
         * @throws JSONException
         */
        private List<Image> getCovers(JSONArray jsonArray) throws JSONException {
            List<Image> covers = new ArrayList<>();
            for (int j = 0; j < jsonArray.length(); j++) {
                JSONObject imageJson = jsonArray.getJSONObject(j);
                Image image = new Image();
                image.url = imageJson.getString("url");
                image.height = imageJson.getInt("height");
                image.width = imageJson.getInt("width");
                covers.add(image);
            }
            return covers;
        }

        /**
         * Create artists string from the Json
         *
         * @param jsonArray contains the artists in Json format
         * @return String
         * @throws JSONException
         */
        private String getArtits(JSONArray jsonArray) throws JSONException {
            List<ArtistSimple> artistSimpleList = new ArrayList<>();
            for (int j = 0; j < jsonArray.length(); j++) {
                JSONObject artistJson = jsonArray.getJSONObject(j);
                ArtistSimple artistSimple = new ArtistSimple();
                artistSimple.name = artistJson.getString("name");
                artistSimpleList.add(artistSimple);
            }
            return ArrayToString.JoinArtistsToString(artistSimpleList);
        }
    }
}
