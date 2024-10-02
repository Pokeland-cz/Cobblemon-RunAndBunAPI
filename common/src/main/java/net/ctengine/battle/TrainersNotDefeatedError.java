package net.ctengine.battle;

import com.cobblemon.mod.common.battles.BattleStartError;
import net.ctengine.trainer.Trainer;
import net.ctengine.trainer.TrainerRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.List;

// Error for when checking the win registry and player has not beaten the requirements
// "Borrowed" from selfdot's code
public class TrainersNotDefeatedError implements BattleStartError {

    private final List<String> trainersNotDefeatedIdList;

    public TrainersNotDefeatedError(List<String> trainersNotDefeatedIdList) {
        this.trainersNotDefeatedIdList = trainersNotDefeatedIdList;
    }

    @NotNull
    @Override
    public MutableText getMessageFor(@NotNull Entity entity) {
        StringBuilder message = new StringBuilder()
                .append("To challenge this trainer, you must have defeated the following trainers: ");

        for (String trainerId : trainersNotDefeatedIdList){
            Trainer trainer = TrainerRegistry.getTrainer(trainerId);
            if (trainer != null){
                message.append(", ").append(trainer.getDisplayName());
            } else {
                message.append(", ").append(trainerId);
            }
        }
        return Text.literal(Formatting.RED + message.toString());
    }

}