package PhotoCompare;

import javax.swing.*;

public class Compare {
    // Получается так, что здесь только запускаем и закрываем форму.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                CompareMain compareMain = new CompareMain();
                compareMain.ShowGUIForm();
                compareMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                compareMain.setVisible(true);
            }
        });
    }
}
