package TextDocumentCollection;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

public class TextDocumentCollectionApp {
    private DocumentManager documentManager;
    private DocumentViewer documentViewer;
    private DocumentEditor documentEditor;
    private Scanner scanner;

    public TextDocumentCollectionApp() {
        this.scanner = new Scanner(System.in);
        this.documentManager = new DocumentManager(Paths.get(System.getProperty("user.dir")));
        this.documentViewer = new DocumentViewer(documentManager, scanner);
        this.documentEditor = new DocumentEditor(documentManager, scanner);
    }

    public void run() {
        System.out.println("Добро пожаловать в приложение 'Коллекция текстовых документов'");
        System.out.println("Текущая корневая папка: " + documentManager.getRootDirectory());

        while (true) {
            printMenu();
            String command = scanner.nextLine().trim();

            try {
                switch (command) {
                    case "1":
                        setRootDirectory();
                        break;
                    case "2":
                        documentManager.listDocuments();
                        break;
                    case "3":
                        documentViewer.openDocument();
                        break;
                    case "4":
                        documentEditor.createNewDocument();
                        break;
                    case "5":
                        documentViewer.searchInDocument();
                        break;
                    case "6":
                        documentEditor.replaceInDocument();
                        break;
                    case "7":
                        documentManager.showDocumentProperties();
                        break;
                    case "8":
                        documentManager.sortDocuments();
                        break;
                    case "9":
                        documentViewer.setPageSize();
                        break;
                    case "10":
                        documentViewer.setOutputMode(!documentViewer.isOutputToFile());
                        break;
                    case "0":
                        System.out.println("Выход из программы...");
                        return;
                    default:
                        System.out.println("Неизвестная команда. Попробуйте снова.");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println("\nМеню:");
        System.out.println("1. Установить корневую папку коллекции");
        System.out.println("2. Показать структуру коллекции");
        System.out.println("3. Открыть документ");
        System.out.println("4. Создать новый документ");
        System.out.println("5. Поиск в документе");
        System.out.println("6. Замена в документе");
        System.out.println("7. Показать свойства документа");
        System.out.println("8. Сортировать коллекцию");
        System.out.println("9. Установить размер страницы");
        System.out.println("10. Переключить вывод (консоль/файл)");
        System.out.println("0. Выход");
        System.out.print("Выберите команду: ");
    }

    private void setRootDirectory() throws IOException {
        System.out.print("Введите путь к новой корневой папке: ");
        String newPath = scanner.nextLine();
        documentManager.setRootDirectory(Paths.get(newPath));
        System.out.println("Корневая папка установлена: " + documentManager.getRootDirectory());
    }
}