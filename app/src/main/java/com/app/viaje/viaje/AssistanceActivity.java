package com.app.viaje.viaje;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class AssistanceActivity extends AppCompatActivity {

    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistance);

        relativeLayout = (RelativeLayout) findViewById(R.id.activity_assistance);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.tire_service_button)
    void onTireService() {

        Snackbar snackbar = Snackbar.make(relativeLayout, "Tire Service Help!", Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.RED);
        snackbar.show();

    }

    @OnClick(R.id.towing_service_button)
    void onTowingService() {

        Snackbar snackbar = Snackbar.make(relativeLayout, "Towing Service Help!", Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.RED);
        snackbar.show();
    }

    @OnClick(R.id.fuel_delivery_service_button)
    void onFuelDelivery() {

        Snackbar snackbar = Snackbar.make(relativeLayout, "Fuel Delivery Service Help!", Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.RED);
        snackbar.show();
    }

    @OnClick(R.id.battery_boost_service_button)
    void onBatteryBoost() {

        Snackbar snackbar = Snackbar.make(relativeLayout, "Battery Boost Service Help!", Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.RED);
        snackbar.show();
    }

    //Butterknife components
    @OnClick(R.id.back_to_menu_button)
    void onBack() {

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

}
