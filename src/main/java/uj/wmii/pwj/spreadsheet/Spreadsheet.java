package uj.wmii.pwj.spreadsheet;

import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Spreadsheet {

    private String[][] sourceSheet;
    private Integer[][] calculatedSheet;


    public String[][] calculate(String[][] sheet) {
        this.sourceSheet = sheet;
        int rows = sheet.length;
        if (rows == 0) {
            return new String[0][0];
        }
        int cols = sheet[0].length;
        this.calculatedSheet = new Integer[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                evaluateCell(i, j);
            }
        }

        String[][] finalResult = new String[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                finalResult[i][j] = String.valueOf(calculatedSheet[i][j]);
            }
        }
        return finalResult;
    }

    private int evaluateCell(int row, int col) {

        if (calculatedSheet[row][col] != null) {
            return calculatedSheet[row][col];
        }

        String cellContent = sourceSheet[row][col].trim();
        int calculatedValue;

        if (cellContent.startsWith("=")) {
            calculatedValue = evaluateFormula(cellContent);
        } else if (cellContent.startsWith("$")) {
            calculatedValue = evaluateReference(cellContent);
        } else {
            calculatedValue = Integer.parseInt(cellContent);
        }

        calculatedSheet[row][col] = calculatedValue;
        return calculatedValue;
    }


    private int evaluateReference(String reference) {

        int col = reference.charAt(1) - 'A';
        int row = Integer.parseInt(reference.substring(2)) - 1;

        return evaluateCell(row, col);
    }


    private int evaluateFormula(String formula) {
        Pattern pattern = Pattern.compile("=([A-Z]{3})\\(([^,]+),([^)]+)\\)");
        Matcher matcher = pattern.matcher(formula);

        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }

        String functionName = matcher.group(1);
        String param1Str = matcher.group(2).trim();
        String param2Str = matcher.group(3).trim();

        int val1 = getParamValue(param1Str);
        int val2 = getParamValue(param2Str);

        BiFunction<Integer, Integer, Integer> operation;

        switch (functionName) {
            case "ADD":
                operation = (a, b) -> a + b;
                break;
            case "SUB":
                operation = (a, b) -> a - b;
                break;
            case "MUL":
                operation = (a, b) -> a * b;
                break;
            case "DIV":
                operation = (a, b) -> a / b;
                break;
            case "MOD":
                operation = (a, b) -> a % b;
                break;
            default:
                throw new IllegalArgumentException("Nieznana funkcja");
        }

        return operation.apply(val1, val2);
    }

    private int getParamValue(String param) {
        if (param.startsWith("$")) {
            return evaluateReference(param);
        } else {
            return Integer.parseInt(param);
        }
    }
}