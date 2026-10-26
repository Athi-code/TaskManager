
package org.classflow.classsession;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/classes")
public class ClassSessionController {

    private final ClassSessionService service;

    public ClassSessionController(ClassSessionService service) {
        this.service = service;
    }

    @PostMapping
    public ClassSession createSession(
            @RequestBody ClassSession session) {
        return service.createSession(session);
    }

    @GetMapping
    public List<ClassSession> getAllSessions() {
        return service.getAllSessions();
    }

    @GetMapping("/{id}")
    public ClassSession getSessionById(
            @PathVariable("id") Long id) {
        return service.getSessionById(id);
    }

    @PatchMapping("/{id}/start")
    public ClassSession recordStart(
            @PathVariable("id") Long id) {
        return service.recordStart(id);
    }

    @PatchMapping("/{id}/confirm-missed")
    public ClassSession confirmMissed(
            @PathVariable("id") Long id) {
        return service.confirmMissed(id);
    }

    @GetMapping("/{id}/delay-impact")
    public String getDelayImpact(
            @PathVariable("id") Long id) {
        return service.getDelayImpact(id);
    }

    @PatchMapping("/{id}/recovery")
    public ClassSession scheduleRecovery(
            @PathVariable("id") Long id,
            @RequestBody RecoveryRequest request) {
        return service.scheduleRecovery(id, request);
    }

    @GetMapping("/{id}/recovery")
    public ClassSession getRecoveryDetails(
            @PathVariable("id") Long id) {
        return service.getRecoveryDetails(id);
    }

    @GetMapping("/{id}/dependency-impact")
    public String getDependencyImpact(
            @PathVariable("id") Long id) {
        return service.getDependencyImpact(id);
    }

@PatchMapping("/{id}/dependency")
public ClassSession setDependency(
        @PathVariable("id") Long id,
        @RequestBody DependencyRequest request) {

    return service.setDependency(id, request.getPrerequisiteId());
}


}
