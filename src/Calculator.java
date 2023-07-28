import org.w3c.dom.ls.LSOutput;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.text.DecimalFormat;

public class Calculator extends JFrame {
    private static JTextArea text;//поле для ввода цифр и знаков
    private double number1 = 0, number2 = 0;//переменные для хранения значений
    private static int oper;//номер операции - индекс символа в массиве NAME_BUTTONS
    private JPanel panelButton; // панель для кнопок
    private final static String NAME_BUTTONS[] =   {"←", "C", "!", "±", "1/x", // массив названий кнопок
                                                    "7", "8", "9", "÷", "√",
                                                    "4", "5", "6", "×", "^",
                                                    "1", "2", "3", "-", "=",
                                                    "0", ".", "+"};
    Listener listener; // слушатель событий кнопок
    static boolean isPressNum = false;  //  кнопки цифр не были нажаты
    static boolean isError = false; // ошибки в вычислениях нет
    boolean isPressDot = false; // точка не нажималась

    Calculator() {
        setJFrame(); // настройка параметров главного окна
        setMenuBar(); // создание и настройка меню
        Container c = getContentPane();  // контейнер
        c.setLayout(null); // расположение панелей в контейнере по координатам
        setTextArea(); // создание и настройка области ввода цифр
        c.add(text); // добавление поля ввода в контейнер
        instalJButtons(); // создание и настройка панели кнопок
        c.add(panelButton); // добавление панели с кнопками
    }

    /** Настройка парметров главного окна   */
    public void setJFrame() {
        setSize(359, 450); // размеры окна
        setTitle("Калькулятор"); // название окна
        setLocationRelativeTo(null); // окно по центру
        setResizable(false);//запрещаем менять размер окна
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//закрытие по крестику
        setVisible(true); // делаем окно видимым
    }

    /** Создание и настройка меню   */
    public void setMenuBar() {
        JMenuBar menuBar = new JMenuBar(); // создание панели меню
        JMenu mainMenu = new JMenu("Главное меню"); // добавление меню
        Font font = new Font("arial", Font.BOLD, 15); // параметры текста меню
        mainMenu.setFont(font); // установка параметров текста
        JMenuItem item1 = new JMenuItem("Выход"); // первый элемент меню
        item1.setFont(font);
        mainMenu.add(item1);
        item1.addActionListener(new ActionListener() {  // слушатель меню "Выход"
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        mainMenu.addSeparator(); // добавление разделителя
        JMenuItem item2 = new JMenuItem("О программе");  // второй элемент меню
        item2.setFont(font);
        mainMenu.add(item2);
        item2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Калькулятор. Версия 2.1.0.0\n" +
                        "© Корпорация Java Home, 2023.\n Все права защищены.");
            }
        });
        menuBar.add(mainMenu);
        setJMenuBar(menuBar);
    }

    /** Создание и настройка панели ввода цифр  */
    public void setTextArea() {
        text = new JTextArea(); // создание поля ввода
        text.setBounds(12, 10, 318, 80); // координаты установки и размер
        text.setFont(new Font("Arial", Font.BOLD, 22));
        text.setLineWrap(true);  // поле не расширяется по ширине
    }

    /** Установка кнопок на панель, настройка   */
    public void instalJButtons() {
        listener = new Listener(); // создание слушателя
        panelButton = new JPanel();
        panelButton.setLayout(null); // без менеджера размещения
        panelButton.setBounds(12, 100, 360, 300);
        JButton buttons[] = new JButton[23];

        int x = 0, y = 0; // начальные координаты установки кнопок
        for (int i = 0; i < 23; i++) {
            if (i != 19 && i != 20) { // для всех кнопок кроме = и 0
                buttons[i] = setButton(); // создаём стандартную кнопку
                buttons[i].setLocation(x, y); // задаём координаты
                buttons[i].setText(NAME_BUTTONS[i]); // присваиваем имя
                panelButton.add(buttons[i]); // добавляем на панель
                x += 65; // смещаем координату вдоль х
            } else if (i == 19) {  // кнопка =
                buttons[i] = setButton();
                buttons[i].setBounds(x, y, 55, 100);
                buttons[i].setText(NAME_BUTTONS[i]);
                panelButton.add(buttons[i]);
            } else { // кнопка 0
                buttons[i] = setButton();
                buttons[i].setBounds(x, y, 120, 45);
                buttons[i].setText(NAME_BUTTONS[i]);
                panelButton.add(buttons[i]);
                x += 130;
            }

            if ((i + 1) % 5 == 0 && i != 0) { // переход на другую строку при размещении кнопок
                y += 55; // если в ряду уже 5 кнопок
                x = 0;
            }
        }
    }

    /** Параметры одной стандартной кнопки
     * @return готовая кнопка с установленным слушателем
     */
    public JButton setButton() {
        var button = new JButton();
        button.setFont(new Font("arial", Font.BOLD | Font.ITALIC, 16));
        button.setForeground(Color.BLACK);
        button.setSize(55, 45);
        button.addActionListener(listener);
        return button;
    }

    /**  Вложенный класс для обработки событий - нажатие кнопок     */
    class Listener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JButton button = (JButton) e.getSource();//получили кнопку, на которую был клик
            switch (button.getText()) {  // проверяем символ на кнопке
                case "+":   // для всех знаков операций
                case "-":
                case "×":
                case "÷":
                case "√":
                case "^":
                case "!":
                case "1/x":
                    // добавляем знак операции, если в тексте нет =, нет ошибки и есть текст
                    if (!isSearchSimvol(text.getText(), 19) && !isError && text.getText().length() > 0) {
                        if (isDigtalLastSimvol(text.getText())) {// проверяем, что последний знак цифра
                            oper = checkOper(button.getText()); // устанавливаем номер операции
                        }
                    }
                    break;
                case "=":
                    if (text.getText().length() > 0 && oper > 0 && // если есть текст и знак операции нажимали
                            (isDigtalLastSimvol(text.getText()) || // перед = допускается цифра, !, √, 1/x
                                    isSearchSimvol(text.getText(), 9) || isSearchSimvol(text.getText(), 2)
                                    || isSearchSimvol(text.getText(), 4))) {

                        double result = workCalc(oper, text.getText()); // запуск вычисления
                        text.append(!isError ? (button.getText() + " " + resultFormat(result).format(result)) : " ");
                        number1 = 0;
                        number2 = 0;
                        oper = 0; // обнуляем знак операции
                        isPressNum = true; // после = цифры не выводим
                        isPressDot = true; // точку не ставим
                    }
                    break;
                case "±":
                    if (text.getText().length() > 0 && !isError) { // если есть текст и нет ошибки вычислений
                        if (!isSearchSimvol(text.getText(), 19)) {// добавляем -, если в тексте нет =
                            if (text.getText().charAt(0) != '-') { // если первый симол не -
                                text.insert("-", 0); // устанавливаем - вперёд
                            } else { // если первый символ -, то перезаписываем подстроку без -
                                text.setText(text.getText().substring(1, text.getText().length()));
                            }
                        }
                    }
                    break;
                case "C":
                    text.setText("");//очищаем текстовое поле
                    number1 = 0;
                    number2 = 0;
                    oper = 0; // номер операции обнуляем
                    isError = false; // убираем ошибки вычислений
                    isPressNum = false; // кнопки цифр снова нажимаем
                    isPressDot = false; // разрешаем добавлять точку
                    break;
                case ".":
                    if (text.getText().length() > 0 && !isError) { // если есть текст в поле и нет ошибки вычислений
                        if (!isPressDot && isDigtalLastSimvol(text.getText())) {// точку можно добавить, если предыдущий символ - цифра,
                            text.append(button.getText());
                            isPressDot = true; //запрет ставить точку
                        }
                    }
                    break;
                case "←": // убираем последний символ, если нет знака =, есть текст и нет ошибки вычислений
                    if (text.getText().length() > 0 && !isSearchSimvol(text.getText(), 19) && !isError) {
                        StringBuilder lastSimvol = new StringBuilder(text.getText().substring(text.getText().length() - 1, (text.getText().length())));
                        if (oper == 2 || oper == 9) { // если удаляли √  или !
                            isPressNum = false; // цифры разрешаем ставить
                        }
                        if (isSearchSimvol(text.getText(), 21)) {// если в тексте есть точка
                            if (text.getText().lastIndexOf(NAME_BUTTONS[oper]) > 0) { // знака операции нет
                                isPressDot = true; //запрет ставить точку
                            } else {
                                isPressDot = false;
                            }
                        }
                        if (NAME_BUTTONS[oper].equals(lastSimvol)) { // удалили какой-то знак операции
                            oper = 0; // знак операции обнулили
                        }
                        text.setText(text.getText().substring(0, text.getText().length() - 1));
                    }
                    break;
                case "0":
                case "1":
                case "2":
                case "3":
                case "4":
                case "5":
                case "6":
                case "7":
                case "8":
                case "9":
                    if (!isPressNum) { // если можно нажимать кнопки цифр
                        text.append(button.getText());
                    }
                    break;
            }
        }
    }

    /** Проверка последнего символа в тексте на то, что это цифра
     *
     * @param text текст на экране калькулятора
     * @return true - последний символ - цифра, false - последний символ - не цифра
     */
    public boolean isDigtalLastSimvol(String text) {
        boolean isDigital = true; // пусть последний символ - цифра
        if (!Character.isDigit(text.charAt(text.length() - 1))) {
            isDigital = false; // последний сивол в строке - не цифра
        }
        return isDigital;
    }

    /** Поиск символа операции в тексте на экране
     *
     * @param text текст на экране калькулятора
     * @param N    номер символа из массива NAME_BUTTONS, который ищем
     * @return true - символ есть в тексте, false - символа нет
     */
    public boolean isSearchSimvol(String text, int N) {
        boolean isSearchSimvol = true; // символ найден
        if (!text.contains(NAME_BUTTONS[N])) {
            isSearchSimvol = false; // символ не найден
        }
        return isSearchSimvol;
    }

    /** Выбор формата вывода результата на экран
     *
     * @param result результат вычислений
     * @return формат вывода числа
     */
    public DecimalFormat resultFormat(double result) {
        DecimalFormat frmt;
        if (result != 0 && (result > 1E6 || Math.abs(result) < 1E-6)) {
            BigDecimal x = new BigDecimal(result);
            frmt = new DecimalFormat("0.00000E0");
        } else {
            frmt = new DecimalFormat("#.###########");
        }
        return frmt;
    }

    /** Назначаем знак операции по номеру символа из массива NAME_BUTTONS
     *
     * @param operZnak символ операции на кнопке
     * @return номер операции
     */
    public int checkOper(String operZnak) {
        if (oper == 0 && text.getText().length() > 0) { // кнопки операций не  нажимали, текста нет
            for (int i = 0; i < NAME_BUTTONS.length; i++) { // ищем знак операции в массиве действий
                if (operZnak.equals(NAME_BUTTONS[i])) {
                    oper = i; // находим и назначаем знак операции
                    if (i == 9 || i == 2 || i == 4) {
                        isPressDot = true;// после √ , 1/x и !  точку не ставим
                        isPressNum = true; // цифры не выводим
                    } else {
                        isPressDot = false;
                    }
                    if (i == 4) { // отдельный вывод для 1/x
                        if (oper == 4 && text.getText().charAt(0) != '-') {
                            text.insert("1/", 0);
                        } else {
                            text.setText("-1/" + text.getText().substring(1, text.getText().length()));
                        }
                    } else { // вывод для всех остальных операций
                        text.append(NAME_BUTTONS[i]); // добавляем в поле
                    }
                    break;
                }
            }
        }
        return oper;
    }

    /** Основной метод вычислений в калькуляторе
     *
     * @param operation номер операции - индекс символа в массиве NAME_BUTTONS
     * @param example   пример - текст на экране калькулятора
     * @return результат вычислений
     */
    public double workCalc(int operation, String example) {
        double result = 0;
        switch (operation) {
            case 22: // сложение
                result = readExample(operation, example)[0] + readExample(operation, example)[1];
                break;
            case 18: // вычитание.
                result = readExample(operation, example)[0] - readExample(operation, example)[1];
                break;
            case 13: // умножение
                result = readExample(operation, example)[0] * readExample(operation, example)[1];
                break;
            case 8: // деление
                result = division(readExample(operation, example)[0], readExample(operation, example)[1]);
                break;
            case 9: // корень квадратный
                number1 = Double.parseDouble(example.substring(0, example.indexOf(NAME_BUTTONS[operation])));
                result = Math.pow(number1, 0.5);
                if (Double.isNaN(result)) { // если число не определено
                    result = incorrectInput(); // выставляем ошибку вычислений
                }
                break;
            case 14: // степень числа
                result = Math.pow(readExample(operation, example)[0], readExample(operation, example)[1]);
                if(Double.isNaN(result) || Double.isInfinite(result)){
                    result = incorrectInput(); // выставляем ошибку вычислений
                    text.append("\nСлишком большое число");
                }
                break;
            case 2: //факториал только целое, положительное число
                result = 1;
                try { // пытаемся считать целое число
                    number1 = Integer.parseInt(example.substring(0, example.indexOf(NAME_BUTTONS[operation])));
                    if (number1 < 0) { // факториал отрицательного числа не считаем, он не определён
                        throw new NumberFormatException();
                    }
                    for (int i = 1; i <= number1; i++) { // цикл вычиления факториала
                        result = result * i;
                    }
                    if (Double.isInfinite(result)) {  // проверка результата на бесконечность
                        result = incorrectInput();
                        text.append("\nПереполнение памяти");
                    }
                } catch (NumberFormatException e) {  // вычисляем факториал только целого числа
                    result = incorrectInput();
                }
                break;
            case 4: // дробь 1/х
                number1 = searchMinus("/", example);
                number2 = Double.parseDouble(example.substring(example.indexOf('/') + 1, example.length()));
                result = division(number1, number2);
                break;
            default:
                result = incorrectInput();
        }
        return result;
    }

    /** Установка параметров при ошибке вычислений
     * @return -1 и параметры ошибки
     */
    public static int incorrectInput() {
        text.setText("Ошибка");
        isError = true; // выставляем ошибку вычислений
        isPressNum = true; // цифры не выводим
        return -1;
    }

    /**
     * Метод считывания чисел для выполнения действий
     *
     * @param operation знак математической операции
     * @param example   текст с экрана калькулятора
     * @return
     */
    public Double[] readExample(int operation, String example) {
        Double[] numbers = new Double[2]; // массив для возврата двух значений для выполнения вычислений
        try {
            numbers[0] = searchMinus(NAME_BUTTONS[operation], example);
            numbers[1] = Double.parseDouble(example.substring(example.lastIndexOf(NAME_BUTTONS[operation]) + 1, example.length()));
        } catch (NumberFormatException e) {
            incorrectInput();
        }
        if (numbers[0] != null && numbers[1] != null) {
            return numbers;
        } else {
            numbers[0] = 0.0;
            numbers[1] = 0.0;
            return numbers;
        }

    }

    /**
     * Определение наличия минуса перед первым числом
     *
     * @param oper    знак операции, до какого считываем число
     * @param example текст на экране калькулятора
     * @return число с минусом или нет
     */
    public double searchMinus(String oper, String example) {
        double number;
        if (example.substring(0, 1).equalsIgnoreCase("−")) { // если минус перед числом
            number = -Double.parseDouble(example.substring(1, example.indexOf(oper))); // считываем с 1 символа до знака операции
        } else {
            try { // пытаемся считать с 0 индекса до знака операции
                number = Double.parseDouble(example.substring(0, example.lastIndexOf(oper)));
            } catch (StringIndexOutOfBoundsException e) {
                incorrectInput(); // если не получилось, то возвращаем 0 с установкой ошибки
                number = 0;
            }

        }
        return number;
    }

    /**
     * Метод деления чисеп с обработкой при делении на 0
     *
     * @param N1 первое число
     * @param N2 второе число
     * @return результат деления или -1, если была попытка разделить на 0
     */
    public double division(double N1, double N2) {
        double res = N1 / N2;
        if (Double.isInfinite(res) || Double.isNaN(res)) {
            text.setText("Деление на 0 невозможно.");
            isError = true; // ошибка вычислений
            return -1;
        } else {
            return res;
        }
    }

    public static void main(String[] args) throws BadLocationException {
        new Calculator();
    }
}
