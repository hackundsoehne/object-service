package edu.kit.ipd.crowdcontrol.objectservice.crowdworking;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.enums.TaskStatus;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PlatformRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TaskRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.PlatformOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.TasksOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by marcel on 22.01.16.
 */
public class PlatformManagerTest {
    private PlatformManager manager;
    private static List<Platform> platforms = new ArrayList<>();

    @BeforeClass
    public static void setUp() {
        platforms = new ArrayList<>();
        platforms.add( new PlatformTest("test1", true, false, false, false));
        platforms.add( new PlatformTest("test2", false, true, false, true));
        platforms.add( new PlatformTest("test3", true, false, true, true));
    }
    @Test
    public void dbInit(){
        Experiment experiment = Experiment.newBuilder()
                .setId(42)
                .build();
        TasksOperations tasksOps = mock(TasksOperations.class);
        PlatformOperations platformOps = mock(PlatformOperations.class);
        WorkerOperations workerOps = mock(WorkerOperations.class);
        TaskRecord rec = new TaskRecord();


        manager = new PlatformManager(platforms,
                param -> Optional.empty(),
                (worker, amount) -> CompletableFuture.completedFuture(true),
                tasksOps,
                platformOps,
                workerOps);

        verify(platformOps).deleteAllPlatforms();

        platforms.forEach(platform -> {
                    //check that every platform got init
                    verify(platformOps).createPlatform(((PlatformTest)platform).toRecord());
        });
        platforms.forEach(platform -> {
            TaskRecord record = new TaskRecord();
            record.setExperiment(42);
            record.setStatus(TaskStatus.running);
            record.setCrowdPlatform(platform.getName());
            record.setPlatformData(42+"");

            when(tasksOps.searchTask(platform.getName(),experiment.getId())).thenReturn(Optional.empty());
            try {
                manager.publishTask(platform.getName(),experiment).
                        map(booleanCompletableFuture -> booleanCompletableFuture.join());
            } catch (TaskOperationException e) {
                e.printStackTrace();
            }
            verify(tasksOps).createTask(record);
        });
        platforms.forEach(platform -> {
            TaskRecord record = new TaskRecord();
            record.setExperiment(42);
            record.setCrowdPlatform(platform.getName());
            record.setStatus(TaskStatus.running);
            record.setPlatformData(42+"");

            when(tasksOps.searchTask(platform.getName(),experiment.getId())).thenReturn(Optional.of(record));
            try {
                manager.updateTask(platform.getName(), experiment).
                        map(booleanCompletableFuture -> booleanCompletableFuture.join());
            } catch (TaskOperationException e) {
                e.printStackTrace();
            }
            verify(tasksOps).updateTask(record);
        });
        platforms.forEach(platform -> {
            TaskRecord record = new TaskRecord();
            record.setExperiment(42);
            record.setCrowdPlatform(platform.getName());
            record.setStatus(TaskStatus.running);
            record.setPlatformData(42+"");

            when(tasksOps.searchTask(platform.getName(),experiment.getId())).thenReturn(Optional.of(record));
            try {
                manager.unpublishTask(platform.getName(), experiment).
                        map(booleanCompletableFuture -> booleanCompletableFuture.join());
            } catch (TaskOperationException e) {
                e.printStackTrace();
            }
            record.setStatus(TaskStatus.finished);
                    verify(tasksOps).updateTask(record);
        });
    }

    static class PlatformTest implements Platform, Payment, Worker {
        private boolean needEmail;
        private boolean handlePayment;
        private boolean handleWorker;
        private boolean renderCalib;
        private String name;

        public PlatformTest(String name, boolean needEmail, boolean handlePayment,
                            boolean renderCalib, boolean handleWorker) {
            this.name = name;
            this.needEmail = needEmail;
            this.handlePayment = handlePayment;
            this.renderCalib = renderCalib;
            this.handleWorker = handleWorker;

        }

        public PlatformRecord toRecord() {
            PlatformRecord record = new PlatformRecord();

            record.setName(name);
            record.setNeedsEmail(needEmail);
            record.setRenderCalibrations(renderCalib);

            return record;
        }

        @Override
        public Optional<Payment> getPayment() {
            if (!handlePayment)
                return Optional.empty();
            else
                return Optional.of(this);
        }

        @Override
        public Optional<Worker> getWorker() {
            if (!handleWorker)
                return Optional.empty();
            else
                return Optional.of(this);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public CompletableFuture<String> publishTask(Experiment experiment) {
            return CompletableFuture.completedFuture(experiment.getId()+"");
        }

        @Override
        public CompletableFuture<Boolean> unpublishTask(String id) {
            return CompletableFuture.completedFuture(true);
        }

        @Override
        public CompletableFuture<String> updateTask(String id, Experiment experiment) {
            return CompletableFuture.completedFuture(id);
        }

        @Override
        public Boolean isCalibsAllowd() {
            return renderCalib;
        }

        @Override
        public CompletableFuture<Boolean> payWorker(Worker worker, int amount) {
            return CompletableFuture.completedFuture(true);
        }

        @Override
        public Optional<String> identifyWorker(Map<String, String[]> param) {
            return Optional.of("50");
        }
    }
}