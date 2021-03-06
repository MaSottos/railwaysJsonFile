package it.uniprisma.exercise4dot2.services;

import com.google.gson.Gson;
import it.uniprisma.exercise4dot2.components.ConfigurationComponent;
import it.uniprisma.exercise4dot2.models.PagedResponse;
import it.uniprisma.exercise4dot2.models.wagon.*;
import it.uniprisma.exercise4dot2.models.wagon.enums.FuelType;
import it.uniprisma.exercise4dot2.models.wagon.enums.WagonClass;
import it.uniprisma.exercise4dot2.utils.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
public class WagonService extends BaseService<Wagon>{

    public WagonService(ConfigurationComponent configurationComponent,
                        Gson gson) {
        super.config = configurationComponent;
        super.gson = gson;
        super.getterMethodOfPrimaryKey = "getId";
    }


    @PostConstruct
    private void initWagon() {
        nameOfClass=Wagon.class.getName();
        try {
            if (!Files.exists(Paths.get(config.getDataPath()))) {
                Files.createDirectory(Paths.get(config.getDataPath()));
            }
            File file = new File(config.getDataPath() + "/wagon.json");
            file.createNewFile();
            ResourceLoader rl = new DefaultResourceLoader();
            resource = rl.getResource("file:" + config.getDataPath() + "/wagon.json");
            if (resource.exists()) {
                Stream<String> lines = Files.lines(resource.getFile().toPath());
                lines.forEach(l -> {
                    l= l.replace("[{", "{");
                    l= l.replace("},", "}");
                    l= l.replace("}]", "}");
                    if (l.contains("MOTOR")) list.add(gson.fromJson(l, MotorWagon.class));
                    else if (l.contains("PASSENGER")) list.add(gson.fromJson(l, PassengerWagon.class));
                    else if (l.contains("BED")) list.add(gson.fromJson(l, BedWagon.class));
                    else if (l.contains("RESTAURANT")) list.add(gson.fromJson(l, RestaurantWagon.class));
                });
            }
        } catch (IOException e) {
            log.error("Error in init of "+Wagon.class.getName());
        }
    }

    public Wagon createSingleWagon(AllOfWagon w) {
        if(w.getWagonType()!=null) {
            switch (w.getWagonType()) {
                case BED -> {
                    return createNew(new BedWagon(w));
                }
                case MOTOR -> {
                    return createNew(new MotorWagon(w));
                }
                case PASSENGER -> {
                    return createNew(new PassengerWagon(w));
                }
                case RESTAURANT -> {
                    return createNew(new RestaurantWagon(w));
                }
            }
        }
        throw new BadRequestException("wagonType can't be null");
    }

    public Wagon updateSingleWagon(AllOfWagon wagon, String wagonId){
        deleteSingle(wagonId);
        return createSingleWagon(wagon);
    }

    public PagedResponse<Wagon> findPage(Boolean bathroom, WagonClass wagonClass, Integer minimumTables, String kitchenType, FuelType fuelType,
                                         Integer minimumBeds, String bedType, Integer minimumSeats, Integer offset, Integer limit) {
        List<Wagon> filtredList = list.stream()
                .filter(w -> {
                    if (bathroom != null) return w.getBathroom()==bathroom;
                    else return true;
                })
                .filter(w -> {
                    if (wagonClass != null) return w.getWagonClass()==wagonClass;
                    else return true;
                })
                .filter(w -> {
                    if (minimumTables != null && w.getClass()== RestaurantWagon.class)
                        return ((RestaurantWagon) w).getNTables()>=minimumTables;
                    else return true;
                })
                .filter(w -> {
                    if (kitchenType != null && w.getClass()== RestaurantWagon.class)
                        return ((RestaurantWagon) w).getKitchenType().equalsIgnoreCase(kitchenType);
                    else return true;
                })
                .filter(w -> {
                    if (fuelType != null && w.getClass()== MotorWagon.class)
                        return ((MotorWagon) w).getFuelType()==(fuelType);
                    else return true;
                })
                .filter(w -> {
                    if (minimumBeds != null && w.getClass()== BedWagon.class)
                        return ((BedWagon) w).getNBeds()>=minimumBeds;
                    else return true;
                })
                .filter(w -> {
                    if (bedType != null && w.getClass()== BedWagon.class)
                        return ((BedWagon) w).getBedType().equalsIgnoreCase(bedType);
                    else return true;
                })
                .filter(w -> {
                    if (minimumSeats != null && w.getClass()== PassengerWagon.class)
                        return ((PassengerWagon) w).getNSeats()>=minimumSeats;
                    else return true;
                })
                .toList();
        return findPage(filtredList, offset, limit);
    }


}
