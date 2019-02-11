package top.sunbread.MCBingo.game;

import org.bukkit.Material;
import top.sunbread.MCBingo.exceptions.NoMaterialException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class BingoGameStatus {

    public static final int SIDE_LENGTH = 5;

    private Material[][] card = new Material[SIDE_LENGTH][SIDE_LENGTH];
    private boolean[][] mark = new boolean[SIDE_LENGTH][SIDE_LENGTH];
    private boolean bingo;

    public BingoGameStatus(Set<Material> materialSet) {
        if (materialSet.size() < SIDE_LENGTH * SIDE_LENGTH) throw new NoMaterialException();
        List<Material> shufflingMaterials = new ArrayList<>(materialSet);
        Collections.shuffle(shufflingMaterials);
        for (int rowIndex = 0; rowIndex < SIDE_LENGTH; ++rowIndex)
            for (int colIndex = 0; colIndex < SIDE_LENGTH; ++colIndex) {
                card[rowIndex][colIndex] = shufflingMaterials.get(rowIndex * SIDE_LENGTH + colIndex);
                mark[rowIndex][colIndex] = false;
            }
        bingo = false;
    }

    public synchronized boolean doMark(Material material) {
        for (int rowIndex = 0; rowIndex < SIDE_LENGTH; ++rowIndex)
            for (int colIndex = 0; colIndex < SIDE_LENGTH; ++colIndex)
                if (card[rowIndex][colIndex] == material) {
                    if (mark[rowIndex][colIndex]) return false;
                    mark[rowIndex][colIndex] = true;
                    if (rowCheck() || colCheck() || majorDiagonalCheck() || minorDiagonalCheck())
                        bingo = true;
                    return true;
                }
        return false;
    }

    public synchronized Material[][] getCard() {
        return card;
    }

    public synchronized boolean[][] getMark() {
        return mark;
    }

    public synchronized boolean isBingo() {
        return bingo;
    }

    private boolean rowCheck() {
        for (int rowIndex = 0; rowIndex < SIDE_LENGTH; ++rowIndex) {
            int counter = 0;
            for (int colIndex = 0; colIndex < SIDE_LENGTH; ++colIndex)
                if (!mark[rowIndex][colIndex]) ++counter;
            if (counter == 0) return true;
        }
        return false;
    }

    private boolean colCheck() {
        for (int colIndex = 0; colIndex < SIDE_LENGTH; ++colIndex) {
            int counter = 0;
            for (int rowIndex = 0; rowIndex < SIDE_LENGTH; ++rowIndex)
                if (!mark[rowIndex][colIndex]) ++counter;
            if (counter == 0) return true;
        }
        return false;
    }

    private boolean majorDiagonalCheck() {
        int counter = 0;
        for (int index = 0; index < SIDE_LENGTH; ++index)
            if (!mark[index][index]) ++counter;
        return counter == 0;
    }

    private boolean minorDiagonalCheck() {
        int counter = 0;
        for (int index = 0; index < SIDE_LENGTH; ++index)
            if (!mark[index][SIDE_LENGTH - 1 - index]) ++counter;
        return counter == 0;
    }

}
