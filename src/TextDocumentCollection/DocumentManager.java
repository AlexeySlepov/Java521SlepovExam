package TextDocumentCollection;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.stream.*;

public class DocumentManager {
    private Path rootDirectory;
    private List<Path> documents;
    private Path currentDocument;

    public DocumentManager(Path rootDirectory) {
        this.rootDirectory = rootDirectory;
        this.documents = new ArrayList<>();
        updateDocumentList();
    }

    public Path getRootDirectory() {
        return rootDirectory;
    }

    public void setRootDirectory(Path newRoot) throws IOException {
        if (!Files.exists(newRoot) || !Files.isDirectory(newRoot)) {
            throw new IOException("Указанный путь не существует или не является папкой");
        }
        this.rootDirectory = newRoot;
        updateDocumentList();
    }

    public void updateDocumentList() {
        try {
            documents = Files.walk(rootDirectory, 1)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".txt"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.out.println("Ошибка при обновлении списка документов: " + e.getMessage());
        }
    }

    public void listDocuments() {
        if (documents.isEmpty()) {
            System.out.println("В коллекции нет документов.");
            return;
        }

        System.out.println("\nКоллекция документов (" + rootDirectory + "):");
        IntStream.range(0, documents.size())
                .forEach(i -> System.out.println((i + 1) + ". " + documents.get(i).getFileName()));
    }

    public Path getDocument(int index) {
        if (index < 0 || index >= documents.size()) {
            throw new IndexOutOfBoundsException("Неверный номер документа");
        }
        currentDocument = documents.get(index);
        return currentDocument;
    }

    public Path getCurrentDocument() {
        if (currentDocument == null) {
            throw new IllegalStateException("Документ не выбран");
        }
        return currentDocument;
    }

    public List<Path> getDocuments() {
        return documents;
    }

    public void showDocumentProperties() throws IOException {
        if (currentDocument == null) {
            System.out.println("Сначала откройте документ");
            return;
        }

        BasicFileAttributes attrs = Files.readAttributes(currentDocument, BasicFileAttributes.class);
        System.out.println("\nСвойства документа:");
        System.out.println("Имя: " + currentDocument.getFileName());
        System.out.println("Размер: " + attrs.size() + " байт");
        System.out.println("Дата создания: " + attrs.creationTime());
        System.out.println("Дата изменения: " + attrs.lastModifiedTime());
    }

    public void sortDocuments() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nСортировать по:");
        System.out.println("1. Имени");
        System.out.println("2. Размеру");
        System.out.println("3. Дате создания");
        System.out.println("4. Автору (не реализовано)");
        System.out.print("Выберите критерий: ");
        String choice = scanner.nextLine();

        Comparator<Path> comparator = null;
        switch (choice) {
            case "1":
                comparator = Comparator.comparing(Path::getFileName);
                break;
            case "2":
                comparator = Comparator.comparing(p -> {
                    try {
                        return Files.size(p);
                    } catch (IOException e) {
                        return 0L;
                    }
                });
                break;
            case "3":
                comparator = Comparator.comparing(p -> {
                    try {
                        return Files.readAttributes(p, BasicFileAttributes.class).creationTime();
                    } catch (IOException e) {
                        return FileTime.fromMillis(0);
                    }
                });
                break;
            case "4":
                System.out.println("Сортировка по автору не реализована");
                return;
            default:
                System.out.println("Неверный выбор");
                return;
        }

        documents.sort(comparator);
        System.out.println("Коллекция отсортирована");
        listDocuments();
    }
}