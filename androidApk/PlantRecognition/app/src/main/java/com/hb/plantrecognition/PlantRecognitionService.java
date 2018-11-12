package com.hb.plantrecognition;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by 译丹 on 2017/5/13.
 */

public interface PlantRecognitionService {
    @POST("plant")
    @FormUrlEncoded
    Call<PlantInfo> plantRecognition(@Field("image") String image);


    class PlantInfo {
        private long log_id;
        private List<Result> result;

        public long getLog_id() {
            return log_id;
        }

        public void setLog_id(long log_id) {
            this.log_id = log_id;
        }

        public List<Result> getResult() {
            return result;
        }

        public void setResult(List<Result> result) {
            this.result = result;
        }
    }

    class Result {
        private String name;
        private float score;
        private Info baike_info;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public float getScore() {
            return score;
        }

        public void setScore(float score) {
            this.score = score;
        }

        public Info getBaike_info() {
            return baike_info;
        }

        public void setBaike_info(Info baike_info) {
            this.baike_info = baike_info;
        }
    }

    class Info {
        private String baike_url;
        private String image_url;
        private String description;

        public String getBaike_url() {
            return baike_url;
        }

        public void setBaike_url(String baike_url) {
            this.baike_url = baike_url;
        }

        public String getImage_url() {
            return image_url;
        }

        public void setImage_url(String image_url) {
            this.image_url = image_url;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}

