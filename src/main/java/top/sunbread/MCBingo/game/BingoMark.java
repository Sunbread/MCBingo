package top.sunbread.MCBingo.game;

public final class BingoMark {

    private boolean[][] mark = new boolean[BingoGameStatus.SIDE_LENGTH][BingoGameStatus.SIDE_LENGTH];
    private boolean bingo;

    public BingoMark() {
        for (int rowIndex = 0; rowIndex < BingoGameStatus.SIDE_LENGTH; ++rowIndex)
            for (int colIndex = 0; colIndex < BingoGameStatus.SIDE_LENGTH; ++colIndex) {
                mark[rowIndex][colIndex] = false;
            }
        bingo = false;
    }

    public boolean doMark(int rowIndex, int colIndex) {
        if (mark[rowIndex][colIndex]) return false;
        mark[rowIndex][colIndex] = true;
        if (rowCheck() || colCheck() || majorDiagonalCheck() || minorDiagonalCheck())
            bingo = true;
        return true;
    }

    public boolean[][] getMark() {
        return mark;
    }

    public boolean isBingo() {
        return bingo;
    }

    private boolean rowCheck() {
        for (int rowIndex = 0; rowIndex < BingoGameStatus.SIDE_LENGTH; ++rowIndex) {
            int counter = 0;
            for (int colIndex = 0; colIndex < BingoGameStatus.SIDE_LENGTH; ++colIndex)
                if (!mark[rowIndex][colIndex]) ++counter;
            if (counter == 0) return true;
        }
        return false;
    }

    private boolean colCheck() {
        for (int colIndex = 0; colIndex < BingoGameStatus.SIDE_LENGTH; ++colIndex) {
            int counter = 0;
            for (int rowIndex = 0; rowIndex < BingoGameStatus.SIDE_LENGTH; ++rowIndex)
                if (!mark[rowIndex][colIndex]) ++counter;
            if (counter == 0) return true;
        }
        return false;
    }

    private boolean majorDiagonalCheck() {
        int counter = 0;
        for (int index = 0; index < BingoGameStatus.SIDE_LENGTH; ++index)
            if (!mark[index][index]) ++counter;
        return counter == 0;
    }

    private boolean minorDiagonalCheck() {
        int counter = 0;
        for (int index = 0; index < BingoGameStatus.SIDE_LENGTH; ++index)
            if (!mark[index][BingoGameStatus.SIDE_LENGTH - 1 - index]) ++counter;
        return counter == 0;
    }

}
