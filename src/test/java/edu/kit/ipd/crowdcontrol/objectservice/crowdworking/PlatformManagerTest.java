package edu.kit.ipd.crowdcontrol.objectservice.crowdworking;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.enums.TaskStatus;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.Task;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PlatformRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TaskRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.PlatformOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.TasksOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Worker;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

/**
 * Created by marcel on 22.01.16.
 */
public class PlatformManagerTest {
    private PlatformManager manager;
    private static List<Platform> platforms = new ArrayList<>();
    private Experiment experiment = Experiment.newBuilder()
            .setId(42)
            .build();
    private TasksOperations tasksOps;
    private PlatformOperations platformOps;
    private WorkerOperations workerOps;

    @BeforeClass
    public static void setUp() {
        platforms = new ArrayList<>();
        platforms.add( new PlatformTest("test1", true, false, false, false));
        platforms.add( new PlatformTest("test2", false, true, false, true));
        platforms.add( new PlatformTest("test3", true, false, true, true));
    }
    @Before
    public void prepare() {
        tasksOps = mock(TasksOperations.class);
        platformOps = mock(PlatformOperations.class);
        workerOps = mock(WorkerOperations.class);

        manager = new PlatformManager(platforms,
                param -> "42",
                (id, experiment1, paymentJob) -> CompletableFuture.completedFuture(true),
                tasksOps,
                platformOps,
                workerOps);
    }
    @Test
    public void dbinit(){
        verify(platformOps).deleteAllPlatforms();

        platforms.forEach(platform -> {
            //check that every platform got init
            verify(platformOps).createPlatform(((PlatformTest)platform).toRecord());
        });
    }
    @Test
    public void publishTest() {
        platforms.forEach(platform -> {
            TaskRecord record = new TaskRecord();
            TaskRecord record2 = new TaskRecord();

            record.setExperiment(42);
            record2.setExperiment(42);

            record.setStatus(TaskStatus.running);
            record2.setStatus(TaskStatus.running);

            record.setCrowdPlatform(platform.getName());
            record2.setCrowdPlatform(platform.getName());

            record2.setPlatformData(42 + "");

            when(tasksOps.createTask(record)).thenReturn(record.copy());
            when(tasksOps.updateTask(record2)).thenReturn(true);

            try {
                manager.publishTask(platform.getName(), experiment).join();
            } catch (TaskOperationException e) {
                e.printStackTrace();
            }

            verify(tasksOps).createTask(record);
            verify(tasksOps).updateTask(record2);
        });
    }

    @Test
    public void updateTest() {
        platforms.forEach(platform -> {
            TaskRecord record = new TaskRecord();
            record.setExperiment(42);
            record.setCrowdPlatform(platform.getName());
            record.setStatus(TaskStatus.running);
            record.setPlatformData(42 + "");

            when(tasksOps.getTask(platform.getName(), experiment.getId())).thenReturn(Optional.of(record));
            try {
                manager.updateTask(platform.getName(), experiment).join();
            } catch (TaskOperationException e) {
                e.printStackTrace();
            }
            verify(tasksOps).updateTask(record);
        });
    }
    @Test
    public void unpublishTask() {
        platforms.forEach(platform -> {
            TaskRecord record = new TaskRecord();
            record.setExperiment(42);
            record.setCrowdPlatform(platform.getName());
            record.setStatus(TaskStatus.running);
            record.setPlatformData(42+"");

            when(tasksOps.getTask(platform.getName(),experiment.getId())).thenReturn(Optional.of(record));
            try {
                manager.unpublishTask(platform.getName(), experiment).join();
            } catch (TaskOperationException e) {
                e.printStackTrace();
            }
            record.setStatus(TaskStatus.finished);
                    verify(tasksOps).updateTask(record);
        });
    }

    static class PlatformTest implements Platform, Payment, WorkerIdentification {
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
        public Optional<WorkerIdentification> getWorker() {
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
        public Boolean isCalibrationAllowed() {
            return renderCalib;
        }

        @Override
        public String identifyWorker(Map<String, String[]> param) {
            return "50";
        }

        @Override
        public CompletableFuture<Boolean> payExperiment(String id, Experiment experiment, List<PaymentJob> paymentJob) {
            return CompletableFuture.completedFuture(true);
        }
    }
}