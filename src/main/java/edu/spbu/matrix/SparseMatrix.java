package edu.spbu.matrix;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

//алгоритм с https://intuit.ru/studies/courses/4447/983/lecture/14931?page=5&ysclid=l13nytqlp8

/**
 * Разряженная матрица
 */
public class SparseMatrix implements Matrix {
    public int height, width;
    public Integer[] CSR_cols;
    public Double[] CSR_value;
    public Integer[] CSR_pointer;

    public SparseMatrix(int height, int width) {
        this.height = height;
        this.width = width;
    }

    /**
     * загружает матрицу из файла
     */
    public SparseMatrix(String fileName) throws IOException {
        this.height = 0;
        this.width = 0;
        Path path = Paths.get(fileName);
        Scanner sc = new Scanner(path);
        //построчно считываем файл
        sc.useDelimiter(System.getProperty("line.separator"));


        //toCSR
        ArrayList<Integer> cols = new ArrayList<>();
        ArrayList<Integer> pointer = new ArrayList<>();
        ArrayList<Double> value2 = new ArrayList<>();
        pointer.add(0);

        int counter = 0;
        while (sc.hasNext()) {
            this.height += 1;
            Scanner sc_string = new Scanner(sc.next());
            sc_string.useLocale(Locale.ENGLISH);


            int i = 0;

            if (this.width == 0) { //если это первая  строка на вход
                while (sc_string.hasNextDouble()) {
                    double number = sc_string.nextDouble();

                    //to CSR
                    if (number != 0) {
                        cols.add(i);
                        value2.add(number);
                        counter += 1;
                    }
                    i += 1;
                }

                pointer.add(counter);

                this.width = i;

            } else { //если последующая
                i = 0;
                while (sc_string.hasNextDouble() && i < this.width) {
                    double number = sc_string.nextDouble();

                    //to CSR
                    if (number != 0) {
                        cols.add(i);
                        value2.add(number);
                        counter += 1;
                    }
                    i += 1;
                }
                //проверка на ошибку:
                if (i != this.width) {
                    throw new IOException("Ошибка во входных данных: " +
                            "несовпадение количества элементов в строке");
                }
                pointer.add(counter);

            }
        }
        sc.close();

        this.CSR_cols = cols.toArray(new Integer[cols.size()]);
        this.CSR_value = value2.toArray(new Double[value2.size()]);
        this.CSR_pointer = pointer.toArray(new Integer[this.height]);
    }

    public SparseMatrix(Integer[] CSR_cols, Double[] CSR_value, Integer[] CSR_pointer, int height, int width) throws IOException {
        this.CSR_cols = CSR_cols;
        this.CSR_value = CSR_value;
        this.CSR_pointer = CSR_pointer;
        this.height = height;
        this.width = width;
    }

    public double getElement(int i, int j) {
        for (int ind = this.CSR_pointer[i]; ind < this.CSR_pointer[i + 1]; ind++) {
            if (this.CSR_cols[ind] == j) {
                return this.CSR_value[ind];
            } else if (this.CSR_cols[ind] > j) {
                break;
            }
        }
        return 0.0;
    }

    public SparseMatrix transposeCSR() throws IOException {
        ArrayList<ArrayList<Integer>> IntVectors = new ArrayList<>(this.width);
        ArrayList<ArrayList<Double>> DoubleVectors = new ArrayList<>(this.width);

        //заполняем пустыми массивами
        for (int i = 0; i < this.width; i++) {
            IntVectors.add(new ArrayList<>());
            DoubleVectors.add(new ArrayList<>());
        }
        //заполняем вспомогательные вектора
        for (int i = 0; i < this.height; i++) {
            for (int k = this.CSR_pointer[i]; k < this.CSR_pointer[i + 1]; k++) {
                IntVectors.get(this.CSR_cols[k]).add(i);
                DoubleVectors.get(this.CSR_cols[k]).add(this.CSR_value[k]);
            }
        }

        ArrayList<Integer> res_cols = new ArrayList<>();
        ArrayList<Double> res_value = new ArrayList<>();
        ArrayList<Integer> res_pointer = new ArrayList<>();
        res_pointer.add(0);

        //пересоздаем CSR
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < DoubleVectors.get(i).size(); j++) {
                res_value.add(DoubleVectors.get(i).get(j));
                res_cols.add(IntVectors.get(i).get(j));
            }
            res_pointer.add(res_pointer.get(i) + DoubleVectors.get(i).size());
        }

        return new SparseMatrix(
                res_cols.toArray(new Integer[res_cols.size()]),
                res_value.toArray(new Double[res_value.size()]),
                res_pointer.toArray(new Integer[res_pointer.size()]),
                this.width, this.height);
    }


    /**
     * однопоточное умнджение матриц
     * должно поддерживаться для всех 4-х вариантов
     *
     * @param m2
     * @return
     */
    public SparseMatrix mul(SparseMatrix m2) throws Exception {
        if (this.width != m2.height) {
            throw new Exception("Не совпадают размеры матриц");
        }
        SparseMatrix m2t = m2.transposeCSR();

        ArrayList<Integer> res_cols = new ArrayList<>();
        ArrayList<Double> res_value = new ArrayList<>();
        ArrayList<Integer> res_pointer = new ArrayList<>();
        res_pointer.add(0);


        int counter = 0;
        for (int i = 0; i < this.height; i++) { //строки в первой матрице
            for (int j = 0; j < m2.width; j++) { //столбцы во второй
                double x;
                double sum = 0;
                int c1 = this.CSR_pointer[i], c2 = m2t.CSR_pointer[j];

                while (c1 < this.CSR_pointer[i + 1] && c2 < m2t.CSR_pointer[j + 1]) { //рассмотрим строку по поинтеру
                    while (m2t.CSR_cols[c2] < CSR_cols[c1] && c2 < m2t.CSR_pointer[j + 1] - 1) {
                        c2++;
                    }
                    if (this.CSR_cols[c1] == m2t.CSR_cols[c2]) {
                        x = this.CSR_value[c1] * m2t.CSR_value[c2];
                        sum += x;
                    }
                    c1++;
                }

                if (sum != 0) {
                    counter++;
                    res_cols.add(j);
                    res_value.add(sum);
                }
            }
            res_pointer.add(counter);
        }

        SparseMatrix res = new SparseMatrix(
                res_cols.toArray(new Integer[res_cols.size()]),
                res_value.toArray(new Double[res_value.size()]),
                res_pointer.toArray(new Integer[res_pointer.size()]),
                this.height, m2.width);

        return res;
    }

    public DenseMatrix mul(DenseMatrix m2) throws Exception {
        if (this.width != m2.height) {
            throw new Exception("Не совпадают размеры матриц");
        }
        Double[][] res = new Double[this.height][m2.width];

        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < m2.width; j++) {
                res[i][j] = 0.0;
                for (int k = 0; k < this.width; k++) {
                    res[i][j] += getElement(i, k) * m2.value[k][j];
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
                        res[I][j] += this.getElement(I,k) * m.value[k][j];
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
                        res[I][j] += this.getElement(I, k)* m.getElement(k,j);
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

    public String toString() {
        StringBuilder matrix = new StringBuilder();
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                matrix.append(this.getElement(i, j));
                matrix.append(" ");
            }
            matrix.append("\n");
        }
        return matrix.toString();
    }


    public static void main(String[] args) throws Exception {

        DenseMatrix matrix1 = new DenseMatrix("m1.txt");
        SparseMatrix matrix2 = new SparseMatrix("m2.txt");
        Matrix matrix = matrix1.mul(matrix2);
        System.out.println(matrix1);
        System.out.println(matrix2);

        System.out.println(matrix);
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
    @Override public Matrix dmul(Matrix o) throws Exception {
        if (o instanceof DenseMatrix){
            return this.dmul((DenseMatrix) o);
        }
        if (o instanceof SparseMatrix){
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
                    if (Math.abs(((DenseMatrix) m).value[i][j] - this.getElement(i, j)) > acc) {
                        return false;
                    }
                }
            }
            return true;
        }
        if (m instanceof SparseMatrix) {
            if (((SparseMatrix) m).CSR_pointer != this.CSR_pointer ||
                    ((SparseMatrix) m).CSR_cols != this.CSR_cols ||
                    ((SparseMatrix) m).CSR_value != this.CSR_value) {
                return false;
            }
            return true;
        }
        return false;
    }
}

