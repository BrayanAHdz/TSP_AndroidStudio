package com.example.tsp;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class TSP {

    private int numLugares;
    private List<Lugar> lugares = new ArrayList<>();
    private int rutas[][];
    private float distancias[];

    private static final float pC = 0.80f;
    private static final float pM = 0.01f;

    public List<Lugar> getTSP(@NonNull List<Lugar> _lugares){
        lugares.clear();

        lugares = _lugares;

        numLugares = lugares.size();
        rutas = new int[10][numLugares];
        distancias = new float[10];

        iniciarPoblacion();
        int mejorRuta[] = ciclo();
        List<Lugar> resultado = new ArrayList<>();

        for (int i = 0; i < numLugares; i++) {
            resultado.add(lugares.get(mejorRuta[i]));
        }

        return resultado;
    }

    private void iniciarPoblacion() {
        for(int i = 0; i < 10; i++){
            rutas[i][0] = 0;
            for(int j = 1; j < numLugares; j++){
                boolean existe;
                int lugar = 0;
                do{
                    existe = false;
                    lugar = (int)(Math.random() * numLugares);
                    for(int k = 0; k < j; k++){
                        if(lugar == rutas[i][k]){
                            existe = true;
                        }
                    }
                }while(existe);
                rutas[i][j] = lugar;
            }
            distancias[i] = calcularDistancia(rutas[i]);
        }
    }

    private float calcularDistancia(int[] ruta) {
        float distancia = 0f;

        for (int i = 0; i < numLugares - 1; i++) {
            float x2 = (float) Math.pow((float) (lugares.get(i).getLatitude() - lugares.get(i + 1).getLatitude()), 2f);
            float y2 = (float) Math.pow((float) (lugares.get(i).getLongitude() - lugares.get(i + 1).getLongitude()), 2f);
            distancia += Math.sqrt(x2 + y2);
        }

        float x2 = (float) Math.pow((float) (lugares.get(numLugares - 1).getLatitude() - lugares.get(0).getLatitude()), 2f);
        float y2 = (float) Math.pow((float) (lugares.get(numLugares - 1).getLongitude() - lugares.get(0).getLongitude()), 2f);
        distancia += Math.sqrt(x2 + y2);

        return distancia;
    }

    private int seleccionTorneo(){
        int part1, part2;

        part1 = (int)(Math.random() * 10);
        do{ part2 = (int)(Math.random() * 10); } while(part1 == part2);

        if(distancias[part1] < distancias[part2]){ return part1; }
        else{ return part2; }
    }



    private int[] ciclo(){

        for(int x = 0; x < 100; x++) {
            for (int z = 0; z < 1000; z++) {
                int padre1Pos, padre2Pos;

                padre1Pos = seleccionTorneo();
                do {
                    padre2Pos = seleccionTorneo();
                } while (padre1Pos == padre2Pos);


                int padre1[] = rutas[padre1Pos];
                int padre2[] = rutas[padre2Pos];

                int hijo1[] = new int[numLugares];
                int hijo2[] = new int[numLugares];

                // Se genera la probabilidad de combinacion por pareja de padres (0, 1)
                float currPC = (float) Math.random();

                // Probabilidad < .80
                // Padre 1: 0,4,3,2,1
                // Padre 2: 0,1,3,4,2
                // Limite = 2
                // Hijo 1 = 0,4,3, ... 1,2
                // Hijo 2 = 0,1,3, ... 4,2

                if (currPC < pC) {
                    int limite = (int) (Math.random() * (numLugares / 2)) + 2;
                    for (int i = 0; i <= limite; i++) {
                        hijo1[i] = padre1[i];
                        hijo2[i] = padre2[i];
                    }

                    boolean contiene = false;
                    int current = limite + 1, current2 = limite + 1;
                    for (int i = 0; i < numLugares; i++) {
                        contiene = false;
                        for (int j = 0; j < limite + 1; j++) {
                            if (padre2[i] == hijo1[j]) {
                                contiene = true;
                                break;
                            }
                        }
                        if (!contiene) {
                            hijo1[current] = padre2[i];
                            current++;
                        }

                        contiene = false;
                        for (int j = 0; j < limite + 1; j++) {
                            if (padre1[i] == hijo2[j]) {
                                contiene = true;
                                break;
                            }
                        }
                        if (!contiene) {
                            hijo2[current2] = padre1[i];
                            current2++;
                        }
                    }
                } else {
                    hijo1 = padre1;
                    hijo2 = padre2;
                }

                // Se genera la probabilidad de mutación para los hijos (0, 1)
                float currPM = (float) Math.random();

                // mutacion < .0.1

                // Hijo 1:
                // pos1 = 1, pos2 = 3
                // Hijo 1 = 0,4,3,1,2 -> 0,1,3,4,2

                // Hijo 2:
                // pos1 = 2, pos2 = 4
                // Hijo 2 = 0,1,3,4,2 -> 0,1,2,4,3

                if (currPM < pM) {

                    int temp, pos1, pos2;

                    pos1 = ((int) (Math.random() * numLugares) % (numLugares - 1)) + 1;
                    do {
                        pos2 = ((int) (Math.random() * numLugares) % (numLugares - 1)) + 1;
                    } while (pos1 == pos2);

                    // Cambio de hijo 1
                    temp = hijo1[pos1];
                    hijo1[pos1] = hijo1[pos2];
                    hijo1[pos2] = temp;

                    pos1 = ((int) (Math.random() * numLugares) % (numLugares - 1)) + 1;
                    do {
                        pos2 = ((int) (Math.random() * numLugares) % (numLugares - 1)) + 1;
                    } while (pos1 == pos2);

                    // Cambio de hijo 2
                    temp = hijo2[pos1];
                    hijo2[pos1] = hijo2[pos2];
                    hijo2[pos2] = temp;

                }

                // Calcular las nuevas distancias de los hijos
                float[] nuevasDistancias = new float[4];
                nuevasDistancias[0] = calcularDistancia(padre1);
                nuevasDistancias[1] = calcularDistancia(padre2);
                nuevasDistancias[2] = calcularDistancia(hijo1);
                nuevasDistancias[3] = calcularDistancia(hijo2);

                int[] pos = {0, 1, 2, 3};

                // Reordenas distancias para obtener las dos mejores
                float aux1;
                int aux2;
                for (int i = 0; i < nuevasDistancias.length - 1; i++) {
                    for (int j = 0; j < nuevasDistancias.length - i - 1; j++) {
                        if (nuevasDistancias[j + 1] < nuevasDistancias[j]) {
                            aux1 = nuevasDistancias[j + 1];
                            nuevasDistancias[j + 1] = nuevasDistancias[j];
                            nuevasDistancias[j] = aux1;

                            aux2 = pos[j + 1];
                            pos[j + 1] = pos[j];
                            pos[j] = aux2;
                        }
                    }
                }

                // Seleccionar el primer mejor para el padre1
                switch (pos[0]) {
                    case 0:
                        rutas[padre1Pos] = padre1;
                        break;
                    case 1:
                        rutas[padre1Pos] = padre2;
                        break;
                    case 2:
                        rutas[padre1Pos] = hijo1;
                        break;
                    case 3:
                        rutas[padre1Pos] = hijo2;
                        break;
                }

                // Seleccionar el segundo mejor para el padre2
                switch (pos[1]) {
                    case 0:
                        rutas[padre2Pos] = padre1;
                        break;
                    case 1:
                        rutas[padre2Pos] = padre2;
                        break;
                    case 2:
                        rutas[padre2Pos] = hijo1;
                        break;
                    case 3:
                        rutas[padre2Pos] = hijo2;
                        break;
                }

                for (int i = 0; i < 10; i++) {
                    distancias[i] = calcularDistancia(rutas[i]);
                }
            }
        }

        // Seleccionar mejor de las rutas
        int mejor = 0;
        for(int i = 0; i < 10; i++){
            if(distancias[mejor] > distancias[i]) {
                mejor = i;
            }
        }

        return rutas[mejor];
    }
}
