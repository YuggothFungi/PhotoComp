package PhotoCompare;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.List;
import java.util.ListIterator;

public class CompareMain extends JFrame {
    private JPanel origPhoto;
    private JPanel editPhoto;
    private JButton chooseEditFolder;
    private JButton chooseOrigFolder;
    private JPanel compareDesktop;
    private JLabel origFolderLabel;
    private JLabel editFolderLabel;
    private JButton prevButton;
    private JButton nextButton;
    private int origFolderSelected = 0;
    private int editFolderSelected = 0;
    private List<String> origFolderFileList;
    private List<String> editFolderFileList;
    private List<String> workingFolder;

    public CompareMain() {
        origFolderLabel.setText("Папка не выбрана");
        editFolderLabel.setText("Папка не выбрана");

        /*
        Обработчики нажатия кнопок выбора папок.
        Смущает, что для обеих кнопок выполняется одинаковая последовательность действий с разницей только в целевой панели и лейблах.
        Кажется, что тут можно написать общий обработчик и звать его с параметром панели.
         */
        chooseOrigFolder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FolderChooser sfc = showFolderChooser();
                int response = sfc.response;
                if (response == 0){ // JFileChooser.APPROVE_OPTION возвращает 0, если пользователь выбрал папку в диалоге
                    origFolderLabel.setText(sfc.path);
                    origFolderSelected = 1;
                    origFolderFileList = GetFileList(sfc.path);
                    if (BothFoldersSelected() == 1){ // проверяем, что обе папки выбраны и запускаем склеивание списка совпадающих файлов
                        workingFolder = GetFolderIntersection(origFolderFileList, editFolderFileList);
                        if (workingFolder != null) {// если список не пустой рисуем первый превью на обеих панелях
                            origPhoto.setToolTipText(workingFolder.get(0));
                            editPhoto.setToolTipText(workingFolder.get(0));
                        }
                        else editPhoto.setToolTipText("Не файлов с одинаковым названием");
                    }
                    else{
                        origPhoto.setToolTipText("Выберите обе папки");
                    }
                }
                else {
                    origFolderLabel.setText("Папка не выбрана");
                    origFolderSelected = 0;
                }
            }
        });

        chooseEditFolder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FolderChooser sfc = showFolderChooser();
                int response = sfc.response;
                if (response == 0){
                    editFolderLabel.setText(sfc.path);
                    editFolderSelected = 1;
                    editFolderFileList = GetFileList(sfc.path);
                    if (BothFoldersSelected() == 1){
                        workingFolder = GetFolderIntersection(origFolderFileList, editFolderFileList);
                        if (workingFolder != null) {// если список не пустой рисуем первый превью на обеих панелях
                            editPhoto.setToolTipText(workingFolder.get(0));
                            origPhoto.setToolTipText(workingFolder.get(0));
                        }
                        else editPhoto.setToolTipText("Не файлов с одинаковым названием");
                    }
                    else{
                        editPhoto.setToolTipText("Выберите обе папки");
                    }
                }
                else {
                    editFolderLabel.setText("Папка не выбрана");
                    editFolderSelected = 0;
                }
            }
        });

        /*
        Тут будут вызовы обработчиков кнопок вперёд-назад и обновление формы. TODO: сделать работающий выриант
        TODO: сделать прокрутку списка по кругу.
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editPhoto.setToolTipText(GetPrevFile(workingFolder));
                origPhoto.setToolTipText(GetPrevFile(workingFolder));
            }
        });
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editPhoto.setToolTipText(GetNextFile(workingFolder));
                origPhoto.setToolTipText(GetNextFile(workingFolder));
            }
        });

         */
    }

    // Создаём форму с размерами и базовой панелью
    public void ShowGUIForm() {
        add(compareDesktop);
        setTitle("New form");
        setSize(1600, 800);
    }

    /*
    Открываем диалог в режиме выбора папки. TODO: запоминать выбор текущей папки.
    Возвращаем в response - выбрана ли папка JFileChooser.APPROVE_OPTION
    Возвращаем в path путь к выбранной папке.
     */
    public FolderChooser showFolderChooser(){
        FolderChooser fc = new FolderChooser();
        final JFileChooser openFileChooser = new JFileChooser();
        openFileChooser.setCurrentDirectory(new File("D:\\Code\\Java\\PhotoCompare"));
        openFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.response = openFileChooser.showOpenDialog(this);
        fc.path = openFileChooser.getSelectedFile().getPath();
        return fc;
    }

    /*
    Возвращаем 1, если папки выбраны на обеих сторонах.
     */
    public int BothFoldersSelected(){
        if (origFolderSelected + editFolderSelected == 2) return 1;
        else return 0;
    }

    /*
    Принимаем путь к выбранной папке. Находим полный путь ко всем файлам в папке (TODO: добавить проверку на то, что это jpg).
    Оставляем только названия файлов. Возвращаем файлы списком.
     */
    public List<String> GetFileList(String FolderPath){
        try (Stream<Path> walk = Files.walk(Paths.get(FolderPath))) {
            List<String> fileString = walk.filter(Files::isRegularFile).map(Path::toString).collect(Collectors.toList());
            List<String> fileList = new ArrayList<>();
            for (String file : fileString){
                fileList.add(file.substring(file.lastIndexOf("\\")+1));
            }
            return fileList;
        }
        catch (IOException ioEx) {
            ioEx.printStackTrace(System.err);
            return null;
        }
    }

    /*
    Принимает на вход два списка файлов из выбранных папок, предполагается, что массив слева (оригинальный всегда
    больше, на всякий случай, делаем проверку TODO: сделать нормальную обработку размеров массивов).
    Создаём копию массива и удаляем из него все элементы, для которых нет совпадения в массиве справа.
    Если массив пустой, возвращаем NULL - TODO: сделать обработчик на NULL при вызове функции.
    Если массив не пустой, возвращаем список имён файлов.
    */
    public List<String> GetFolderIntersection(List<String> origFileList, List<String> editFileList){
        List<String> modFileList;
        modFileList = new ArrayList<>(origFileList);
        if (origFileList.size() >= editFileList.size()) {
            for (int i = modFileList.size() -1; i > -1; --i) { //идём по индексам с конца до 0
                String str = modFileList.get(i);
                if (!editFileList.remove(str))
                    modFileList.remove(str);
            }
        }
        if (modFileList.size() > 0) return modFileList;
        else return null;
    }

    /*
    // Планирую сделать обработчики нажатия вперёд-назад по списку совпадающих файлов. Пока что-то не получается.
    public String GetNextFile(List<String> fileList){
        ListIterator<String> lit = fileList.listIterator();

        if (lit.hasNext()) {
             return lit.next();
        }
        else return null; //
    }

    public String GetPrevFile(List<String> fileList){
        ListIterator<String> lit = fileList.listIterator();

        if (lit.hasPrevious()) {
            return lit.previous();
        }
        else return null;
    }
    */
}