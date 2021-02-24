package test;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.Comparator;
import java.util.Random;

public class TestApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private final Callback<SuperBean, Observable[]> extractor = param -> {
        var adapter = new SuperAdapter(param);
        return new Observable[]{adapter};
    };
    private final ObservableList<SuperBean> items = FXCollections.observableArrayList(extractor);
    private final SortedList<SuperBean> sortedItems = new SortedList<>(items, Comparator.comparing(SuperBean::toString));

    @Override
    public void start(Stage primaryStage) {
        items.addAll(
                new SuperBean(),
                new SuperBean(),
                new SuperBean(),
                new SuperBean()
        );

        VBox vbox = new VBox();
        Scene scene = new Scene(vbox);
        primaryStage.setScene(scene);
        createTestUI(vbox);

        primaryStage.show();
    }

    private void createTestUI(VBox vbox) {
        var superList = new ListView<SuperBean>();
        vbox.getChildren().add(superList);
        superList.setItems(sortedItems);
        var subList = new ListView<Bean>();
        vbox.getChildren().add(subList);
        superList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Selection changed: " + oldValue + " | " + newValue);
            if (newValue == null) {
                subList.setItems(null);
            } else {
                subList.setItems(newValue.beans);
            }
        });
        superList.getSelectionModel().select(0);

        var button = new Button("Change value");
        vbox.getChildren().add(button);
        button.setOnAction(event -> {
            //changed Object property
            items.get(0).beans.get(0).property.set(getRandomString());
        });
    }

    private static class SuperBean {
        private final Callback<Bean, Observable[]> extractor = param -> {
            var adapter = new Adapter(param);
            return new Observable[]{adapter};
        };
        ObservableList<Bean> beans = FXCollections.observableArrayList(extractor);

        SuperBean() {
            beans.addAll(
                    new Bean("A"),
                    new Bean("B"),
                    new Bean("C"),
                    new Bean("D")
            );
        }

        @Override
        public String toString() {
            return beans.toString();
        }
    }

    private static class SuperAdapter extends ObservableValueBase<SuperBean> {

        private final SuperBean item;

        SuperAdapter(SuperBean mItem) {
            item = mItem;
            item.beans.addListener((InvalidationListener) observable -> fireValueChangedEvent());
        }

        @Override
        public SuperBean getValue() {
            return item;
        }

    }

    private static class Bean {
        SimpleStringProperty property = new SimpleStringProperty();

        Bean(String string) {
            property.set(string);
        }

        @Override
        public String toString() {
            return property.getValue();
        }
    }

    private static class Adapter extends ObservableValueBase<Bean> {

        private final Bean item;

        Adapter(Bean mItem) {
            item = mItem;
            item.property.addListener(c -> fireValueChangedEvent());
        }

        @Override
        public Bean getValue() {
            return item;
        }

    }

    public String getRandomString() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }


}
