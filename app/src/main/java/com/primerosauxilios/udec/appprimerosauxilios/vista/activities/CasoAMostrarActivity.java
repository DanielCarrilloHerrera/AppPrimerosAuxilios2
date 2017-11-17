package com.primerosauxilios.udec.appprimerosauxilios.vista.activities;

import android.media.MediaPlayer;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.primerosauxilios.udec.appprimerosauxilios.R;
import com.primerosauxilios.udec.appprimerosauxilios.logica.Aplicacion;
import com.primerosauxilios.udec.appprimerosauxilios.logica.Caso;
import com.primerosauxilios.udec.appprimerosauxilios.persistencia.DatabasePAConstantes;

public class CasoAMostrarActivity extends AppCompatActivity {
    private static int unTiempo = 0;
    private ImageButton btnPlayPause;
    private Thread ActualizarTiempoAudio = new Hilo();
    private Caso caso;
    private Handler manejador;
    private String nombreCaso;
    private boolean reproduciendoAudio;
    private MediaPlayer reproductor;
    private int tiempoAdelante = 5000;
    private double tiempoComienzo = 0;
    private int tiempoDetras = 5000;
    private double tiempoFinal = 0;
    private TextView tvTexto;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caso);
        this.tvTexto = (TextView) findViewById(R.id.tvTexto);
        btnPlayPause = (ImageButton) findViewById(R.id.btnPlayPause);
        this.nombreCaso = getIntent().getStringExtra(DatabasePAConstantes.CASO);
        cargarCaso();
        String audio = this.caso.getAudioProcedimiento().toLowerCase();

        if(VERSION.SDK_INT <= 22)
            this.reproductor = MediaPlayer.create(this, getResources().getIdentifier(audio, "raw", getApplicationContext().getPackageName()));
        else
            this.reproductor = MediaPlayer.create(this, getResources().getIdentifier(audio.substring(0, audio.length() - 4), "raw", getApplicationContext().getPackageName()));

        this.reproduciendoAudio = false;
        this.manejador = new Handler();
    }

    private void cargarCaso() {
        this.caso = Aplicacion.getInstancia(getApplicationContext()).getCaso(this.nombreCaso);
        setTitle(this.caso.getNombre());
        if (VERSION.SDK_INT >= 24) {
            this.tvTexto.setText(Html.fromHtml(this.caso.getProcedimiento(), 0));
        } else {
            this.tvTexto.setText(Html.fromHtml(this.caso.getProcedimiento()));
        }
    }

    public void botonesAudio(View view) {
        switch (view.getId()) {
            case R.id.btnRetroceder:
                if (((int) this.tiempoComienzo) - this.tiempoDetras > 0) {
                    this.tiempoComienzo -= (double) this.tiempoDetras;
                    this.reproductor.seekTo((int) this.tiempoComienzo);
                    return;
                }
                return;
            case R.id.btnPlayPause:
                if (this.reproduciendoAudio) {
                    Toast.makeText(getApplicationContext(), "Pausando",Toast.LENGTH_SHORT).show();
                    this.reproductor.pause();
                    reproduciendoAudio = false;
                    btnPlayPause.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    return;
                }
                else {
                    Toast.makeText(getApplicationContext(), "Reproduciendo audio...", Toast.LENGTH_SHORT).show();
                    this.reproductor.start();
                    this.tiempoFinal = (double) this.reproductor.getDuration();
                    this.tiempoComienzo = (double) this.reproductor.getCurrentPosition();
                    if (unTiempo == 0) {
                        unTiempo = 1;
                    }
                    reproduciendoAudio = true;
                }
                this.manejador.postDelayed(this.ActualizarTiempoAudio, 100);
                btnPlayPause.setImageResource(R.drawable.ic_pause_black_24dp);
                return;
            case R.id.btnAdelantar:
                if (((double) (this.tiempoAdelante + ((int) this.tiempoComienzo))) <= this.tiempoFinal) {
                    this.tiempoComienzo += (double) this.tiempoAdelante;
                    this.reproductor.seekTo((int) this.tiempoComienzo);
                    return;
                }
                return;
            default:
                return;
        }
    }

    protected void onStop() {
        super.onStop();
        this.ActualizarTiempoAudio.interrupt();
        this.ActualizarTiempoAudio = null;
        this.reproductor.stop();
    }

    class Hilo extends Thread {
        private boolean ejecutarse = true;

        Hilo() {
        }

        public void run() {
            if (!this.ejecutarse) {
                CasoAMostrarActivity.this.tiempoComienzo = (double) CasoAMostrarActivity.this.reproductor.getCurrentPosition();
            }
            CasoAMostrarActivity.this.manejador.postDelayed(this, 100);
        }
    }
}
