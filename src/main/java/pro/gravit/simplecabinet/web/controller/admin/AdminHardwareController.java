package pro.gravit.simplecabinet.web.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import pro.gravit.simplecabinet.web.dto.HardwareInfoDto;
import pro.gravit.simplecabinet.web.dto.PageDto;
import pro.gravit.simplecabinet.web.exception.EntityNotFoundException;
import pro.gravit.simplecabinet.web.model.HardwareId;
import pro.gravit.simplecabinet.web.service.HardwareIdService;

import java.util.Base64;

@RestController
@RequestMapping("/admin/hardware")
public class AdminHardwareController {
    @Autowired
    private HardwareIdService hardwareIdService;

    @GetMapping("/id/{id}")
    public HardwareInfoDto findById(@PathVariable long id) {
        var hardwareOptional = hardwareIdService.findById(id);
        if (hardwareOptional.isEmpty()) {
            throw new EntityNotFoundException("HardwareId not found");
        }
        return new HardwareInfoDto(hardwareOptional.get());
    }

    @PostMapping("/id/{id}/setpublickey")
    public void setPublicKey(@PathVariable long id, @RequestBody SetPublicKeyRequest request) {
        var hardwareOptional = hardwareIdService.findById(id);
        if (hardwareOptional.isEmpty()) {
            throw new EntityNotFoundException("HardwareId not found");
        }
        var hardware = hardwareOptional.get();
        hardware.setPublicKey(Base64.getDecoder().decode(request.publicKey));
        hardwareIdService.save(hardware);
    }

    @GetMapping("/publickey/{publicKey}")
    public HardwareInfoDto findByPublicKey(@PathVariable String publicKey) {
        byte[] pubKey = Base64.getUrlDecoder().decode(publicKey);
        var hardwareOptional = hardwareIdService.findByPublicKey(pubKey);
        if (hardwareOptional.isEmpty()) {
            throw new EntityNotFoundException("HardwareId not found");
        }
        return new HardwareInfoDto(hardwareOptional.get());
    }

    @PostMapping("/search")
    public HardwareInfoDto findByData(@RequestBody HardwareSearchRequest request) {
        var hardwareOptional = hardwareIdService.findByHardware(request);
        if (hardwareOptional.isEmpty()) {
            throw new EntityNotFoundException("HardwareId not found");
        }
        return new HardwareInfoDto(hardwareOptional.get());
    }

    @GetMapping("/page/{pageId}")
    public PageDto<HardwareInfoDto> getPage(@PathVariable int pageId) {
        var result = hardwareIdService.findAll(PageRequest.of(pageId, 10));
        return new PageDto<>(result.map(HardwareInfoDto::new));
    }

    @PutMapping("/new")
    public HardwareInfoDto create(@RequestBody HardwareCreateRequest request) {
        var hw = new HardwareId();
        hw.setBitness(request.bitness());
        hw.setTotalMemory(request.totalMemory());
        hw.setLogicalProcessors(request.logicalProcessors());
        hw.setPhysicalProcessors(request.physicalProcessors());
        hw.setProcessorMaxFreq(request.processorMaxFreq());
        hw.setBattery(request.battery());
        hw.setHwDiskId(request.hwDiskId());
        hw.setDisplayId(Base64.getDecoder().decode(request.displayId()));
        hw.setBaseboardSerialNumber(request.baseboardSerialNumber());
        hw.setPublicKey(Base64.getDecoder().decode(request.publicKey()));
        hardwareIdService.save(hw);
        return new HardwareInfoDto(hw);
    }

    public record HardwareCreateRequest(int bitness,
                                        long totalMemory,
                                        int logicalProcessors,
                                        int physicalProcessors,
                                        long processorMaxFreq,
                                        boolean battery,
                                        String hwDiskId,
                                        String displayId,
                                        String baseboardSerialNumber, String publicKey) {
    }

    public record HardwareSearchRequest(int bitness,
                                        long totalMemory,
                                        int logicalProcessors,
                                        int physicalProcessors,
                                        long processorMaxFreq,
                                        boolean battery,
                                        String hwDiskId,
                                        String displayId,
                                        String baseboardSerialNumber) {
    }

    public record SetPublicKeyRequest(String publicKey) {
    }


}
