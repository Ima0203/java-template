package edu.spbu.matrix;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Плотная матрица
 */
public class DenseMatrix implements Matrix {
    public int height, width;
    public Double[][] value;

    public DenseMatrix(int height, int width) {
        this.height = height;
        this.width = width;
    }

    /**
     * загружает матрицу из файла
     */
    public DenseMatrix(String fileName) throws IOException {
        this.height = 0;
        this.width = 0;
        Path path = Paths.get(fileName);
        Scanner sc = new Scanner(path);
        //построчно считываем файл
        sc.useDelimiter(System.getProperty("line.separator"));

        //создание итогового массива
        ArrayList<Double[]> matrix = new ArrayList<>();

        while (sc.hasNext()) {
            this.height += 1;
            Scanner sc_string = new Scanner(sc.next());
            sc_string.useLocale(Locale.ENGLISH);
            ArrayList<Double> row = new ArrayList<>();

            if (this.width == 0) { //если это первая  строка на вход
                while (sc_string.hasNextDouble()) {
                    double number = sc_string.nextDouble();
                    row.add(number);
                }
                this.width = row.size();

            } else { //если последующая
                int i = 0;
                while (sc_string.hasNextDouble() && i < this.width) {
                    double number = sc_string.nextDouble();
                    row.add(number);
                    i += 1;
                }
                //проверка на ошибку:
                if (i != this.width) {
                    System.out.println("Ошибка во входных данных: " +
                            "несовпадение количества элементов в строке");
                }
            }
            matrix.add(row.toArray(new Double[this.width]));
        }
        sc.close();

        this.value = matrix.toArray(new Double[this.height][this.width]);
    }


    public String toString() {
        StringBuilder matrix = new StringBuilder();
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                matrix.append(this.value[i][j]);
                matrix.append(" ");
            }
            matrix.append("\n");
        }
        return matrix.toString();
    }


    public void stdout() {
        for (int i = 0; i < this.height; i++) {
            for (int k = 0; k < this.width; k++) {
                System.out.print(this.value[i][k]);
                System.out.print(" ");
            }
            System.out.println();
        }
    }


    public static void main(String[] args) throws Exception {

        SparseMatrix matrix1 = new SparseMatrix("m1.txt");
        DenseMatrix matrix2 = new DenseMatrix("m2.txt");
        Matrix matrix = matrix1.dmul(matrix2);
        System.out.println(matrix);
        System.out.println(matrix2 == matrix2);
    }


    /**
     * однопоточное умнджение матриц
     * должно поддерживаться для всех 4-х вариантов
     *
     * @param matrix1
     * @return
     */

    public DenseMatrix mul(DenseMatrix matrix1) throws Exception {
        if (this.width != matrix1.height) {
            throw new Exception("Не совпадают размеры матриц");
        }
        Double[][] res = new Double[this.height][matrix1.width];

        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < matrix1.width; j++) {
                res[i][j] = 0.0;
                for (int k = 0; k < this.width; k++) {
                    res[i][j] += this.value[i][k] * matrix1.value[k][j];
                }
            }
        }
        DenseMatrix matrix = new DenseMatrix(this.height, matrix1.width);
        matrix.value = res;
        matrix.height = this.height;
        matrix.width = matrix1.width;
        return matrix;
    }

    public DenseMatrix mul(SparseMatrix m2) throws Exception {
        if (this.width != m2.height) {
            throw new Exception("Не совпадают размеры матриц");
        }
        Double[][] res = new Double[this.height][m2.width];

        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < m2.width; j++) {
                res[i][j] = 0.0;
                for (int k = 0; k < this.width; k++) {
                    res[i][j] += m2.getElement(k, j) * value[i][k];
                }
            }
        }
        DenseMatrix matrix = new DenseMatrix(this.height, m2.width);
        matrix.value = res;
        matrix.height = this.height;
        matrix.width = m2.width;
        return matrix;
    }

    private DenseMatrix dmul(DenseMatrix m) throws Exception {
        if (this.width != m.height) {
            throw new Exception("Не совпадают размеры матриц");
        }
        Double[][] res = new Double[this.height][m.width];

        ExecutorService executorService = Executors.newCachedThreadPool();
        ArrayList<Future> tasks = new ArrayList<>();

        for (int i = 0; i < this.height; i++) {//выбрали строку
            int I = i;
            tasks.add(executorService.submit(() -> { //подаем функцию в качестве арг.
                for (int j = 0; j < m.width; j++) { //выбрали столбец
                    res[I][j] = 0.0;
                    for (int k = 0; k < this.width; k++) { //элементы столбца
                        res[I][j] += this.value[I][k] * m.value[k][j];
                    }
                }
            }));
        }
        DenseMatrix matrix = new DenseMatrix(this.height, m.width);
        matrix.value = res;
        return matrix;
    }

    private DenseMatrix dmul(SparseMatrix m) throws Exception {
        if (this.width != m.height) {
            throw new Exception("Не совпадают размеры матриц");
        }
        Double[][] res = new Double[this.height][m.width];

        ExecutorService executorService = Executors.newCachedThreadPool();
        ArrayList<Future> tasks = new ArrayList<>();

        for (int i = 0; i < this.height; i++) {//выбрали строку
            int I = i;
            tasks.add(executorService.submit(() -> { //подаем функцию в качестве арг.
                for (int j = 0; j < m.width; j++) { //выбрали столбец
                    res[I][j] = 0.0;
                    for (int k = 0; k < this.width; k++) { //элементы столбца
                        res[I][j] += this.value[I][k] * m.getElement(k,j);
                    }
                }
            }));
        }
        for (Future task: tasks) { //ожидание выполнения на каждом потоке
            task.get();
        }

        executorService.shutdown();

        DenseMatrix matrix = new DenseMatrix(this.height, m.width);
        matrix.value = res;
        return matrix;
    }

    @Override
    public Matrix mul(Matrix m2) throws Exception {
        if (m2 instanceof DenseMatrix) {
            return this.mul((DenseMatrix) m2);
        } else if (m2 instanceof SparseMatrix) {
            return this.mul((SparseMatrix) m2);
        } else return null;
    }

    /**
     * многопоточное умножение матриц
     *
     * @param o
     * @return
     */
    @Override
    public Matrix dmul(Matrix o) throws Exception {
        if (o instanceof DenseMatrix) {
            return this.dmul((DenseMatrix) o);
        }
        if (o instanceof SparseMatrix) {
            return this.dmul((SparseMatrix) o);
        }
        return null;
    }


    /**
     * сравнивает с обоими вариантами
     *
     * @param m
     * @return
     */
    @Override
    public boolean equals(Object m) {
        double acc = 1e-8;
        if (m instanceof DenseMatrix) {
            for (int i = 0; i < this.height; i++) {
                for (int j = 0; j < this.width; j++) {
                    if (Math.abs(((DenseMatrix) m).value[i][j] - this.value[i][j]) > acc) {
                        return false;
                    }
                }
            }
            return true;
        }
        if (m instanceof SparseMatrix) {
            for (int i = 0; i < this.height; i++) {
                for (int j = 0; j < this.width; j++) {
                    if (Math.abs(this.value[i][j] - ((SparseMatrix) m).getElement(i, j)) > acc) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

}
